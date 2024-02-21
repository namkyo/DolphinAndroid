package com.gnbsoftec.dolphinnative.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.CookieManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
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
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.ImgUtil
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
        onCreateImpl()
    }

    private fun onCreateImpl(){
        GLog.d("+++++++++++++++++++++++++++++++[ WebViewActivity : Android BuildConfig ]+++++++++++++++++++++++++++++++")
        GLog.d("BuildConfig.BUILD_TYPE                  : " + BuildConfig.BUILD_TYPE)
        GLog.d("Constants.IS_REAL                       : " + Constants.IS_REAL)
        GLog.d("Constants.IS_DEBUG                      : " + Constants.IS_DEBUG)
        GLog.d("Constants.launchUrl                     : " + Constants.getWebViewHost())
        GLog.d("PUSH_KEY                                : " + PreferenceUtil.getValue(context, PreferenceUtil.keys.PUSH_KEY , ""))
        GLog.d("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        val extras = intent.extras
        GLog.d("data : Key: ${intent.data}")
        extras?.let {
            for (key in it.keySet()) {
                val value = it.get(key)
                GLog.d("extras : Key: $key, Value: $value")
            }
        }
        intent?.getStringExtra(PreferenceUtil.keys.PUSH_URL)?.let {
            GLog.d("푸쉬 데이터 $it")
            PreferenceUtil.put(context,PreferenceUtil.keys.LINK_DATA,it)
        }

        //JSESSIONID 만 지우고 나머지 쿠키 유지
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            // 웹뷰 호스트의 쿠키를 가져옵니다.
            val cookieManager = CookieManager.getInstance()
            cookieManager.getCookie(Constants.getWebViewHost())?.let { cookies ->
                // 쿠키 정보를 로그에 출력합니다.
                GLog.d("Webview:cookies = $cookies")

                // 모든 쿠키를 제거합니다. 콜백을 통해 성공 여부를 로그에 출력합니다.
                cookieManager.removeAllCookies { success ->
                    GLog.d("쿠키 지우기 성공: $success")
                }

                // 쿠키 문자열을 ";"로 분리하여 배열로 만듭니다.
                val arrCookie = cookies.split(";")

                // 분리된 쿠키 각각에 대해 조건을 검사합니다.
                arrCookie.forEach { cookie ->
                    // 'JSESSIONID'와 'DOLPHIN_INTR'이 포함되지 않은 쿠키는 다시 설정합니다.
                    if (cookie.contains("JSESSIONID=").not()
                        //&& cookie.contains("DOLPHIN_INTR=").not()
                    ){
                        val cookieName = cookie.split("=")[0].trim() + "=" // 쿠키 이름을 추출하고 값은 비웁니다.
                        GLog.d("쿠키 다시 설정: $cookieName")
                        cookieManager.setCookie(Constants.getWebViewHost(), cookieName)
                    }
                }
            }
        }

        binding.webView.loadUrl(Constants.getWebViewHost())

        //앱오픈 검사
        loadTimer()
    }

    //앱실행중
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        GLog.d("onNewIntent")
        val extras = intent?.extras
        GLog.d("data : Key: ${intent?.data}")
        extras?.let {
            for (key in it.keySet()) {
                val value = it.get(key)
                GLog.d("extras : Key: $key, Value: $value")
            }
        }

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
        activity.runOnUiThread {
            binding.progressBar.visibility = View.GONE

            // 애니메이션이 끝나면 서서히 사라지게 처리
            runOnUiThread {
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