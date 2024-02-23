package com.gnbsoftec.dolphinnative.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.databinding.ActivityWebViewBinding
import com.gnbsoftec.dolphinnative.extension.toJsonString
import com.gnbsoftec.dolphinnative.manager.CameraManager
import com.gnbsoftec.dolphinnative.manager.InputFileManager
import com.gnbsoftec.dolphinnative.manager.PickerManager
import com.gnbsoftec.dolphinnative.util.BackPressUtil
import com.gnbsoftec.dolphinnative.util.CoroutineUtil
import com.gnbsoftec.dolphinnative.util.FileUtil
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.PermissionUtil
import com.gnbsoftec.dolphinnative.util.PreferenceUtil
import com.gnbsoftec.dolphinnative.util.ToastUtil
import com.gnbsoftec.dolphinnative.util.WebViewUtil
import com.gnbsoftec.dolphinnative.web.WebAppInterface
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class WebViewActivity : BaseActivity<ActivityWebViewBinding>(R.layout.activity_web_view) {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GLog.d("+++++++++++++++++++++++++++++++[ WebViewActivity : Android BuildConfig ]+++++++++++++++++++++++++++++++")
        GLog.d("BuildConfig.BUILD_TYPE                  : " + BuildConfig.BUILD_TYPE)
        GLog.d("Constants.IS_REAL                       : " + Constants.IS_REAL)
        GLog.d("Constants.IS_DEBUG                      : " + Constants.IS_DEBUG)
        GLog.d("Constants.launchUrl                     : " + Constants.getWebViewHost())
        GLog.d("PUSH_KEY                                : " + PreferenceUtil.getValue(context, PreferenceUtil.keys.PUSH_KEY , ""))
        GLog.d("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        onCreateImpl()
    }

    private fun onCreateImpl(){
        //푸쉬 클릭하고 왔는지 체크 있으면 푸쉬 파라미터 적재
        intent?.getStringExtra(PreferenceUtil.keys.PUSH_URL)?.let {
            GLog.d("푸쉬 데이터 $it")
            PreferenceUtil.put(context,PreferenceUtil.keys.LINK_DATA,it)
        }
        //쿠키 초기화
        WebViewUtil.cookieInit(Constants.getWebViewHost())
        //url 이동
        binding.webView.loadUrl(Constants.getWebViewHost())
        //앱오픈 검사
        loadTimer()
    }

    //앱실행중
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        GLog.d("onNewIntent")
        //앱이 열려있는중 푸쉬 알림 클릭시 바로 링크 이동
        intent?.getStringExtra(PreferenceUtil.keys.PUSH_URL)?.let {
            val callScript = "javascript:NativeUtil.fnPushLink(JSON.parse(JSON.stringify(${it.toJsonString()})));"
            GLog.d("푸쉬 링크 이동 $callScript")
            loadURL(callScript)
        }
    }

    private fun loadTimer(){

        var timeIndex = 0
        lifecycleScope.launch {
            CoroutineUtil.setInterval(1000L) {
                ++timeIndex
                GLog.e("앱 오픈 검증 탐지중..$timeIndex")
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

    @SuppressLint("JavascriptInterface")
    override fun initView() {
        //웹뷰셋팅
        binding.webView.apply {
            WebViewUtil.setupWebView(this)
            webViewClient = WebViewUtil.setupWebViewClient(context){
                dolphinApplication.isWebViewLoad = true
            }
            /**
             * 다운로드 리스너
             */
            setDownloadListener {url, userAgent, contentDisposition, mimetype, contentLength ->
                GLog.d("다운로드 리스너 $url")
                //상대경로 처리
                val fileUrl = if(url.startsWith("http")){
                    url
                }else{
                    Constants.getWebViewHost()+url
                }
                FileUtil.urlFileDownload(activity,fileUrl){result,msg->
                    GLog.d("다운로드 결과 result=$result , msg=$msg")
                }
//                FileUtil.fileDownload(activity,url, userAgent, contentDisposition, mimetype, contentLength)
            }
            webChromeClient = WebViewUtil.setupWebChromeClient(context, (activity as WebViewActivity))
            addJavascriptInterface(WebAppInterface(activity), Constants.callScript)
            addJavascriptInterface(WebAppInterface(activity), "ftBridge")
        }
    }

    override fun initListener() {
        //뒤로가기
        onBackPressedDispatcher.addCallback(this@WebViewActivity, BackPressUtil.webView(binding.webView,"javascript:NativeUtil.fnBackBtn();"))

        //갤러리 이동 후 처리
        imagePickerLauncher = intentResult{resultCode,data->PickerManager.resultPicker(activity,resultCode,data)}
        //카메라 이동 후 처리
        cameraLauncher = intentResult{resultCode,data->CameraManager.resultCamera(activity,resultCode,data)}

        fileChooserLauncher = intentResult{resultCode,data->InputFileManager.resultPInputFile(activity,resultCode,data)}

    }

    fun mainPermission(list: List<String>,callback: (Boolean) -> Unit){
        PermissionUtil.requestPermission(context,permissionResult,list,callback)
    }

    fun openGallery() {
        imagePickerLauncher.launch(Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        })
    }
    fun openCamera() {
        cameraLauncher.launch(Intent(activity,CameraView::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        })
    }

    fun openInputFile(){
//        fileChooserLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
        fileChooserLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            type = "image/*" // 오직 이미지 파일만 선택 가능
        })
    }

    /**
     * 콜백 함수 실행
     */
    fun loadURL(url: String) {
        binding.webView.post {
            GLog.d("loadURL = $url")
            binding.webView.loadUrl(url)
        }
    }

    /**
     * 통신완료후 앱 오픈 처리
     */
    private fun appOpen(){
        runOnUiThread {
            //로딩제거
            binding.progressBar.visibility = View.GONE

            // 애니메이션이 끝나면 서서히 사라지게 처리
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

        }
    }


    override fun onResume() {
        super.onResume()
        loadURL("javascript:NativeUtil.fnForeGound();")
    }
    override fun onPause() {
        super.onPause()
        loadURL("javascript:NativeUtil.fnBackGound();")
    }

    fun appClose(){
        Handler(Looper.getMainLooper()).postDelayed({
            activity.finishAffinity()
            exitProcess(0)
        }, 2000)
    }
}