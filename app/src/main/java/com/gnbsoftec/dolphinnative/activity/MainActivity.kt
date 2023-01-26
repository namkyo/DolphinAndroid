package com.gnbsoftec.dolphinnative.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.bridge.NativeBridge
import com.gnbsoftec.dolphinnative.config.Constants
import com.gnbsoftec.dolphinnative.databinding.ActivityMainBinding
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertBindingInterface
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertDialog
import com.gnbsoftec.dolphinnative.fragment.CustomDialogConfirmBindingInterface
import com.gnbsoftec.dolphinnative.fragment.CustomDialogConfirmDialog
import com.gnbsoftec.dolphinnative.util.BackPressUtil
import com.gnbsoftec.dolphinnative.util.BitmapUtil
import com.gnbsoftec.dolphinnative.util.BitmapUtil.createImageFile
import com.gnbsoftec.dolphinnative.util.Logcat
import com.gnbsoftec.dolphinnative.util.SharedPreferenceHelper
import org.json.JSONObject

class MainActivity : BaseActivity() {
    //Data 바인딩
    private lateinit var binding : ActivityMainBinding
    //컨테이너
    private lateinit var activity: Activity
    // 저장소
    private lateinit var sp : SharedPreferenceHelper

    //콜백(성공)
    private var succFunc = ""
    //콜백(실패)
    private var failFunc = ""

    private var mWebViewImageUpload: ValueCallback<Array<Uri>>? = null
    private var downloadId : Long = 0

    //첨부파일 결과처리
    private lateinit var onShowFileChooserlauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        activity = this
        sp = SharedPreferenceHelper(applicationContext)
        //뷰 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뷰 초기화
        init(binding)

        val urlStr = if(TextUtils.isEmpty(intent.extras?.getString(Constants.pushIntentData.pushUrl))){
            Constants.SERVER_URL
        }else{
            intent.extras?.getString(Constants.pushIntentData.pushUrl)!!
        }
        Logcat.d("urlStr : $urlStr")

        binding.webview.loadUrl(urlStr)
    }


    private fun init(binding: ActivityMainBinding){
        //웹뷰 셋팅
        webViewSetting(binding.webview)
        //뒤로가기 셋팅
        onBackPressedDispatcher.addCallback(this,BackPressUtil.webView(binding.webview,binding))
        //첨부파일 셋팅
        onShowFileChooserlauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Logcat.d("첨부파일 콜백 : ${result.resultCode}")

            if (result.resultCode == RESULT_OK) {
                result.data?.let {
                    if(TextUtils.isEmpty(it.getStringExtra("imgUrl"))){
                        it.data.let {uri->
                            mWebViewImageUpload!!.onReceiveValue(arrayOf(uri!!))
                        }
                    }else{
                        val imgUrl = Uri.parse(it.getStringExtra("imgUrl"))
                        val results = arrayOf(imgUrl)
                        mWebViewImageUpload!!.onReceiveValue(results)
                    }
                }
            }
            else{ //취소 한 경우 초기화
                mWebViewImageUpload?.onReceiveValue(null)
                mWebViewImageUpload = null
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSetting(webView: WebView){
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            //첨부파일 다운로드
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                Logcat.d("================setDownloadListener=================")
                Logcat.d("url = $url")
                Logcat.d("userAgent = $userAgent")
                Logcat.d("contentDisposition = $contentDisposition")
                Logcat.d("mimetype = $mimetype")
                Logcat.d("contentLength = $contentLength")

                val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                // 파일명 정제 시작
                val urlSplit = url.split("/")
                val fileName = urlSplit[(urlSplit.size-1)]
                // 파일명 정제 끝

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading File")
                    setAllowedOverMetered(true)//모바일네트워크가 연결되었을 때도 다운로드
                    setAllowedOverRoaming(true)
                    setTitle(fileName)
                    setRequiresCharging(false)
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    allowScanningByMediaScanner()
                    val fileSubPath = "/GNB/$fileName"
                    Logcat.d("fileSubPath : $fileSubPath")
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileSubPath)
                    //content://downloads/public_downloads
                }

                registerDownloadReceiver(downloadManager, activity)
                try {
                    downloadId = downloadManager.enqueue(request)
                    showToast("다운로드 시작중..")
                }catch (e:Exception){
                    e.printStackTrace()
                    Logcat.e(e.stackTraceToString())
                    showToast("다운로드 에러")
                }
            }
            
            //웹페이지에서 일어나는 액션들에 관한 콜백함수
            webChromeClient = object : WebChromeClient() {
                //window.open 시
                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                    val newWebView = WebView(activity)
                    val transport = resultMsg!!.obj as WebView.WebViewTransport
                    transport.webView = newWebView
                    resultMsg.sendToTarget()
                    newWebView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val browserIntent = Intent(Intent.ACTION_VIEW, request.url)
                            startActivity(browserIntent)
                            return true
                        }

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(browserIntent)
                            return true
                        }
                    }
                    return true
                }

                //확인창
                override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                    val dialog = CustomDialogAlertDialog(object :
                        CustomDialogAlertBindingInterface {
                        override fun onYesButtonClick() {
                            try {
                                result.confirm()
                            } catch (e: Exception) {
                                showToast("${e.message}")
                            }
                        }
                    }, message)
                    // 알림창이 띄워져있는 동안 배경 클릭 막기
                    dialog.isCancelable = false
                    dialog.show(supportFragmentManager, "ConfirmAlertDialog")

                    return true
                }

                //선택창
                override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                    val dialog = CustomDialogConfirmDialog(object :
                        CustomDialogConfirmBindingInterface {
                        override fun onYesButtonClick() {
                            try {
                                result.confirm()
                            } catch (e: Exception) {
                                showToast("${e.message}")
                            }
                        }
                        override fun onNoButtonClick() {
                            try {
                                result.cancel()
                            } catch (e: Exception) {
                                showToast("${e.message}")
                            }
                        }
                    }, message,"취 소","확 인")
                    // 알림창이 띄워져있는 동안 배경 클릭 막기
                    dialog.isCancelable = false
                    dialog.show(supportFragmentManager, "ConfirmConfirmDialog")
                    return true
                }

                //첨부파일
                @SuppressLint("QueryPermissionsNeeded")
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    try{
                        mWebViewImageUpload = filePathCallback!!

                        val dialog = CustomDialogConfirmDialog(object :
                            CustomDialogConfirmBindingInterface {
                            override fun onYesButtonClick() {
                                //일반카메라 cameraX
                                val takePictureIntent = Intent(activity,CameraActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                }
                                onShowFileChooserlauncher.launch(takePictureIntent)
                            }
                            override fun onNoButtonClick() {
                                //앨범
                                val contentSelectionIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                                    type = "image/*"
                                }
                                onShowFileChooserlauncher.launch(contentSelectionIntent)
                            }
                        }, "골라주세요","앨 범","일반 카메라")
                        // 알림창이 띄워져있는 동안 배경 클릭 막기
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "ConfirmConfirmDialog")
                    }
                    catch (e : Exception){e.printStackTrace()}
                    return true
                }
            }

            //웹페이지를 로딩할때 생기는 콜백함수
            webViewClient = object : WebViewClient(){
                //페이지 로드시 스플레쉬 화면
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Logcat.d("onPageStarted")
                    super.onPageStarted(view, url, favicon)
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    if(binding.splashView.visibility == View.VISIBLE){
                        binding.splashView.animate()
                            .alpha(0.0f)
                            .setDuration(1200)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    binding.splashView.visibility = View.GONE
                                }
                            }).start()
                    }
                    super.onPageFinished(view, url)
                }
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
            addJavascriptInterface(NativeBridge(activity), "ftBridge")
        }
    }

    /**
     * FCM 토큰 주기
     */
    fun getToken(params: String,succFunc: String, failFunc: String) {
        Logcat.d("getToken.params = $params")
        this.succFunc=succFunc
        this.failFunc=failFunc
        try {
            val token = sp.getValue("token","")
            JSONObject().apply {
                put("msg","정상")
                put("token",token)
            }.let {
                binding.webview.succFunc("0000",it)
            }
        }catch (e:Exception){
            JSONObject().apply {
                put("msg","에러")
                put("token","")
                put("errMsg","${e.message}")
            }.let {
                binding.webview.failFunc("9999",it)
            }
        }
    }

    fun toast(params: String,succFunc: String, failFunc: String){
        this.succFunc=succFunc
        this.failFunc=failFunc
        val jsonObject = JSONObject(params)
        val msg = try{
            jsonObject.getString("MSG")
        }catch (e:Exception){
            "MSG 을 채워주세요."
        }
        showToast(msg)
    }

    /**
     * appData 실행
     */
    fun appData(params: String, succFunc: String, failFunc: String) {

        val jsonObject = JSONObject(params)
        val gubun = jsonObject.optString("gubun", "")

        val resData = JSONObject()

        when (gubun) {
            "C" -> { // 등록
                val keys = try{
                    jsonObject.getJSONObject("reqData")
                }catch (e:Exception){
                    JSONObject()
                }
                val i = keys.keys() // 키 추출
                while (i.hasNext()) {
                    val key = i.next().toString()
                    val value = keys.getString(key)
                    sp.getValue(key,value)
                }
            }
            "R" -> { // 조회
                val keys = try{
                    jsonObject.optString("reqData", "").replace(" ", "").split(",")
                }catch (e:Exception){
                    ArrayList()
                }
                for(key in keys){
                    resData.put(key,sp.getValue(key,""))
                }
            }
            else -> {
                showToast("작업구분 C or R 로 조회해주세요.")
            }
        }

        try {
            binding.webview.succFunc("0000",resData)
        }catch (e:Exception){
            binding.webview.failFunc("9999",resData)
        }
    }

    /**
     * 웹뷰 성공 콜백
     */
    private fun WebView.succFunc(resultCd:String,params:JSONObject){
        JSONObject().apply {
            put(R.string.callBackResultCd.toString(),resultCd)
            put(R.string.callBackParams.toString(),params)
        }.let {
            val strJavaScript = "$succFunc($it)"
            Logcat.d("succFunc : $strJavaScript")
            runOnUiThread{
                this.loadUrl("${R.string.callBackBegin}$strJavaScript")
            }
        }
    }
    /**
     * 웹뷰 실패 콜백
     */
    private fun WebView.failFunc(resultCd:String,params:JSONObject){
        JSONObject().apply {
            put(R.string.callBackResultCd.toString(),resultCd)
            put(R.string.callBackParams.toString(),params)
        }.let {
            val strJavaScript = "$failFunc($it)"
            Logcat.d("failFunc : $strJavaScript")
            runOnUiThread{
                this.loadUrl("${R.string.callBackBegin}$strJavaScript")
            }
        }
    }

    /**
     * 다운로드 상태 리시버
     */
    private fun registerDownloadReceiver(downloadManager: DownloadManager, activity: Activity) {
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                when (intent?.action) {
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                        if(downloadId == id){
                            val query: DownloadManager.Query = DownloadManager.Query()
                            query.setFilterById(id)
                            val cursor = downloadManager.query(query)
                            if (!cursor.moveToFirst()) {
                                return
                            }
                            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                //갤러리 갱신
                                activity.sendBroadcast(
                                    Intent(
                                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.parse("file://" + Environment.DIRECTORY_DOWNLOADS+"/GNB/")
                                    )
                                )
                                showToast("다운로드가 완료됐습니다.")
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                showToast("다운로드가 실패했습니다.")
                            }
                        }
                    }
                }
            }
        }
        IntentFilter().run {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            activity.registerReceiver(downloadReceiver, this)
        }
    }
}