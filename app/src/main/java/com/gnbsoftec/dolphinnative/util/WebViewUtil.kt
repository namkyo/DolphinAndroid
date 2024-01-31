package com.gnbsoftec.dolphinnative.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertBindingInterface
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object WebViewUtil {
    var appOpen = false

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(webView: WebView) {
        // 웹뷰 설정을 위한 WebSettings 인스턴스 가져오기
        webView.settings.apply {
            // JavaScript 활성화
            javaScriptEnabled = true

            setSupportMultipleWindows               (true)  //새창 띄우기 허용
            // 확대/축소 컨트롤 활성화
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false // 화면에 확대/축소 컨트롤러를 표시하지 않음

            // 캐시 모드 설정
            cacheMode = Constants.webViewCacheMode

            // 기타 옵션 설정
            setSupportMultipleWindows               (true)  //새창 띄우기 허용
            domStorageEnabled = true // 로컬 스토리지 및 세션 스토리지 사용
            loadWithOverviewMode = true // 컨텐츠가 웹뷰 너비에 맞게 조정됨
            useWideViewPort = true // 메타 태그의 "viewport" 설정을 사용
            textZoom                                = 100   //시스템 글꼴의 의해 변환 방지
            allowFileAccess                         = true
            allowContentAccess                      = true
            loadWithOverviewMode                    = true  //html컨텐츠가 웹뷰보다 클경우 스크린 크기에 맞게 조정
            useWideViewPort                         = true  //html의 viewport 메타 태그 지원
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW   //https,http 호환여부
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O_MR1){
                safeBrowsingEnabled                     =   true
            }
        }
    }

    fun setupWebViewClient(activity: Activity, appOpenFunc: () -> Unit): WebViewClient {
        return object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Glog.d("onPageStarted : $url")
                LoadingUtil.showLoading(activity)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                Glog.d("onPageFinished : $url")

                //페이지가 로드가 완료시 앱 오픈처리
                if(!appOpen){
                    appOpenFunc()
                    appOpen=true
                }
                LoadingUtil.hideLoading()
            }

            /**
             * 모바일 키패드 리스너
             */
            override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent?) {
                val keyCode = "${event?.keyCode}"
                Glog.d("onUnhandledKeyEvent.keyCode = $keyCode")

                /**
                 * 모바일키패드 이동,엔터 시 키패드 닫기
                 */
                if(event?.keyCode == KeyEvent.KEYCODE_ENTER){
                    val imm = activity.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                }
                return
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                Glog.e("ssL 서버에러 : $error")

                val msg = when(error?.primaryError){
                    SslError.SSL_EXPIRED->"[SSL_EXPIRED] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_IDMISMATCH->"[SSL_IDMISMATCH] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_NOTYETVALID->"[SSL_NOTYETVALID] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_UNTRUSTED->"[SSL_UNTRUSTED] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    else -> "[${error?.primaryError}] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                }
                Glog.d("SSL 에러 : $msg")
                Glog.d("SSL 에러 : ${view?.url}")
            }
        }
    }

    /**
     * 웹뷰 UI 리스너 기능
     * 안씀
     */
    fun setupWebChromeClient(context:Context): WebChromeClient {
        return object: WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val allowMultiple = fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE
//                tempFilePathCallback = filePathCallback
//                tempAllowMultiple = allowMultiple
//                requestPermission(null, filePathCallback, allowMultiple)
                return true
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                if(view != null){
                    val subWV = WebView(context)
                    setupWebView(subWV)

                    subWV.tag = 9999
                    val dialog = Dialog(context)
                    dialog.setContentView(subWV)
                    dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT)
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                    subWV.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean { return false }
                    }
                    subWV.webChromeClient = object: WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
                            dialog.dismiss()
                            window?.destroy()
                            super.onCloseWindow(window)
                        }
                    }
                    dialog.show()
                    val tran:WebView.WebViewTransport = resultMsg?.obj as WebView.WebViewTransport
                    tran.webView = subWV
                    resultMsg.sendToTarget()
                    return true
                }
                return true
            }
            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
            }
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Glog.d("${consoleMessage?.message()}")
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                AlertUtil.showAlert(context,"안내",message,"확인"){
                    result.confirm()
                }
                return true
            }
            override fun onJsConfirm(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                AlertUtil.showConfirm(context,"안내",message,"확인","취소",mOkCallback = {
                    result.confirm()
                }, mCancelCallback = {
                    result.cancel()
                })
                return true
            }

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                val et = EditText(context)
                et.setSingleLine()
                et.setText(defaultValue)
                MaterialAlertDialogBuilder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
                    .setMessage(message)
                    .setView(et)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok) { i, d ->
                        result?.confirm(et.text.toString())
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        result?.cancel()
                    }
                    .show()
                return true
            }
        }
    }
}