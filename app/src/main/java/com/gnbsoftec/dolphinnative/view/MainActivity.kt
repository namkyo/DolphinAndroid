package com.gnbsoftec.dolphinnative.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.view.View
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.web.NativeBridge
import com.gnbsoftec.dolphinnative.databinding.ActivityMainBinding
import com.gnbsoftec.dolphinnative.util.BackPressUtil
import com.gnbsoftec.dolphinnative.util.CoroutineUtil
import com.gnbsoftec.dolphinnative.util.Glog
import com.gnbsoftec.dolphinnative.util.PreferenceUtil
import com.gnbsoftec.dolphinnative.util.ToastUtil
import com.gnbsoftec.dolphinnative.util.WebViewUtil
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
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

        Glog.d("+++++++++++++++++++++++++++++++[ MainActivity : Android BuildConfig ]+++++++++++++++++++++++++++++++")
        Glog.d("BuildConfig.BUILD_TYPE                  : " + BuildConfig.BUILD_TYPE)
        Glog.d("Constants.IS_REAL                       : " + Constants.IS_REAL)
        Glog.d("Constants.IS_DEBUG                      : " + Constants.IS_DEBUG)
//        Glog.d("Constants.launchUrl                     : " + Constants.getWebViewHost())
        Glog.d("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")

        val urlStr = if(TextUtils.isEmpty(intent.extras?.getString(Constants.pushIntentData.pushUrl))){
            Constants.SERVER_URL
        }else{
            intent.extras?.getString(Constants.pushIntentData.pushUrl)!!
        }
        Glog.d("urlStr : $urlStr")

        binding.webView.loadUrl(urlStr)

        val token = PreferenceUtil.getValue(context,"token","")
        Glog.d("token : $token")

        loadTimer()
    }

    private fun loadTimer(){

        var timeIndex = 0
        lifecycleScope.launch {
            CoroutineUtil.setInterval(1000L) {
                ++timeIndex
                Glog.e("앱 오픈 검증 탐지중..$timeIndex")
                //정상 앱 오픈
                if(dolphinApplication.isWebViewLoad){
                    cancel()
                    appOpen()
                }

                //타임이 계속되면 토스트 보여주기
                if(timeIndex % 10 == 0){
                    ToastUtil.show(activity,"인터넷 연결을 확인해주세요.",3000)
                }

                //최대 180초
                if(timeIndex>180){
                    cancel()
                    ToastUtil.show(activity,"앱을 종료합니다.",3000)
                    appClose()
                }
            }
        }
    }


    /**
     * 통신완료후 앱 오픈 처리
     */
    private fun appOpen(){
        binding.progressBar.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.bgBack.animate()
                .alpha(0f)
                .setDuration(500) // 0.5초 동안 알파값을 0으로 변경
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.bgBack.visibility = View.GONE
                    }
                })
            binding.splashAnim.animate()
                .alpha(0f)
                .setDuration(500) // 0.5초 동안 알파값을 0으로 변경
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.splashAnim.visibility = View.GONE
                    }
                })

        }, 500)
    }


    override fun initView(){
        //웹뷰셋팅
        binding.webView.apply {
            WebViewUtil.setupWebView(this)
            webViewClient = WebViewUtil.setupWebViewClient(activity){
                dolphinApplication.isWebViewLoad = true
            }
            webChromeClient = WebViewUtil.setupWebChromeClient(context)
//            addJavascriptInterface(WebAppInterface(activity), Constants.callScript)
            addJavascriptInterface(NativeBridge(activity), "ftBridge")
        }
    }

    override fun initListener() {
        //뒤로가기
        onBackPressedDispatcher.addCallback(this@MainActivity, BackPressUtil.webView(binding.webView,binding))

        //첨부파일 셋팅
        onShowFileChooserlauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Glog.d("첨부파일 콜백 : ${result.resultCode}")

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


    /**
     * FCM 토큰 주기
     */
    fun getToken(params: String,succFunc: String, failFunc: String) {
        Glog.d("getToken.params = $params")
        this.succFunc=succFunc
        this.failFunc=failFunc
        try {
            val token = PreferenceUtil.getValue(context,"token","")
            JSONObject().apply {
                put("msg","정상")
                put("token",token)
            }.let {
                binding.webView.succFunc("0000",it)
            }
        }catch (e:Exception){
            JSONObject().apply {
                put("msg","에러")
                put("token","")
                put("errMsg","${e.message}")
            }.let {
                binding.webView.failFunc("9999",it)
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
                    PreferenceUtil.getValue(context,key,value)
                }
            }
            "R" -> { // 조회
                val keys = try{
                    jsonObject.optString("reqData", "").replace(" ", "").split(",")
                }catch (e:Exception){
                    ArrayList()
                }
                for(key in keys){
                    resData.put(key,PreferenceUtil.getValue(context,key,""))
                }
            }
            else -> {
                showToast("작업구분 C or R 로 조회해주세요.")
            }
        }

        try {
            binding.webView.succFunc("0000",resData)
        }catch (e:Exception){
            binding.webView.failFunc("9999",resData)
        }
    }

    /**
     * 웹뷰 성공 콜백
     */
    private fun WebView.succFunc(resultCd:String,params:JSONObject){
        JSONObject().apply {
            put("resultCd",resultCd)
            put("params",params)
        }.let {
            val strJavaScript = "$succFunc($it)"
            Glog.d("succFunc : $strJavaScript")
            runOnUiThread{
                val callBackUrl = "javascript:$strJavaScript"
                Glog.d("callBackUrl : $callBackUrl")
                this.loadUrl(callBackUrl)
            }
        }
    }
    /**
     * 웹뷰 실패 콜백
     */
    private fun WebView.failFunc(resultCd:String,params:JSONObject){
        JSONObject().apply {
            put("resultCd",resultCd)
            put("params",params)
        }.let {
            val strJavaScript = "$failFunc($it)"
            Glog.d("failFunc : $strJavaScript")
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


    fun appClose(){
        Handler(Looper.getMainLooper()).postDelayed({
            activity.finishAffinity()
            exitProcess(0)
        }, 2000)
    }
}