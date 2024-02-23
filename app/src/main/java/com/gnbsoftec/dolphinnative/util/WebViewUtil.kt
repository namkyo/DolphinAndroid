package com.gnbsoftec.dolphinnative.util

import android.annotation.SuppressLint
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
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.manager.InputFileManager
import com.gnbsoftec.dolphinnative.view.WebViewActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLEncoder

object WebViewUtil {
    var appOpen = false

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(webView: WebView) {

        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

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

            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW //https, http 호환 여부(https에서 http컨텐츠도 보여질수 있도록 함)

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
        cookieManager.setAcceptThirdPartyCookies(webView, false)
    }

    fun setupWebViewClient(context: Context, appOpenFunc: () -> Unit): WebViewClient {
        return object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                GLog.d("onPageStarted : $url")
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                GLog.d("onPageFinished : $url")

                //페이지가 로드가 완료시 앱 오픈처리
                if(!appOpen){
                    appOpenFunc()
                    appOpen=true
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                GLog.d("URL 들어옴 1 ${view.url}")
                return false
            }

            /**
             * 모바일 키패드 리스너
             */
            override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent?) {
                val keyCode = "${event?.keyCode}"
                GLog.d("onUnhandledKeyEvent.keyCode = $keyCode")

                /**
                 * 모바일키패드 이동,엔터 시 키패드 닫기
                 */
                if(event?.keyCode == KeyEvent.KEYCODE_ENTER){
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                }
                return
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                val errorCode = error.errorCode
                val description = when (errorCode) {
                    ERROR_AUTHENTICATION -> "서버에서 사용자 인증 실패"
                    ERROR_BAD_URL -> "잘못된 URL"
                    ERROR_CONNECT -> "서버로 연결 실패"
                    ERROR_FAILED_SSL_HANDSHAKE -> "SSL 핸드셰이크 실패"
                    ERROR_FILE -> "일반 파일 오류"
                    ERROR_FILE_NOT_FOUND -> "파일을 찾을 수 없음"
                    ERROR_HOST_LOOKUP -> "서버 또는 프록시 호스트 이름 조회 실패"
                    ERROR_IO -> "서버에서 읽거나 서버로 쓰기 실패"
                    ERROR_PROXY_AUTHENTICATION -> "프록시에서 사용자 인증 실패"
                    ERROR_REDIRECT_LOOP -> "너무 많은 리디렉션"
                    ERROR_TIMEOUT -> "연결 시간 초과"
                    ERROR_TOO_MANY_REQUESTS -> "페이지가 너무 많은 요청을 보냄"
                    ERROR_UNKNOWN -> "일반 오류"
                    ERROR_UNSUPPORTED_AUTH_SCHEME -> "지원되지 않는 인증 체계"
                    ERROR_UNSUPPORTED_SCHEME -> "URL 스키마가 지원되지 않음"
                    else -> "알 수 없는 오류"
                }
                GLog.e("onReceivedError errorCode : $errorCode , description : $description")
//                if(errorCode != ERROR_UNKNOWN){
//                    AlertUtil.showAlert(context,"안내","[$errorCode]$description url : ${request.url}","재접속"){
//                        view.reload()
//                    }
//                }
            }
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                // 상태 코드와 이유 구절을 로깅
                GLog.e("onReceivedHttpError errorCode: ${errorResponse?.statusCode}, description: ${errorResponse?.reasonPhrase}")
                if (request?.isForMainFrame == true) {
                    when (errorResponse?.statusCode) {
                        503, 404 -> {
                            // 웹뷰에서 assets/www/error.html 페이지 로드
                            val errorCode = errorResponse.statusCode
                            val errorMessage = errorResponse.reasonPhrase ?: "No error message provided."
                            val errorUrl = "file:///android_asset/www/데이터 처리ml?errorCode=$errorCode&errorMessage=${URLEncoder.encode(errorMessage, "UTF-8")}"
                            view?.loadUrl(errorUrl)
                        }
                    }
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                GLog.e("ssL 서버에러 : $error")

                val msg = when(error?.primaryError){
                    SslError.SSL_EXPIRED->"[SSL_EXPIRED] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_IDMISMATCH->"[SSL_IDMISMATCH] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_NOTYETVALID->"[SSL_NOTYETVALID] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    SslError.SSL_UNTRUSTED->"[SSL_UNTRUSTED] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                    else -> "[${error?.primaryError}] 이 사이트의 보안 인증서는 신뢰할수 없습니다."
                }
                GLog.d("SSL 에러 : $msg")
                GLog.d("SSL 에러 : ${view?.url}")
            }
        }
    }

    /**
     * 웹뷰 UI 리스너 기능
     * 안씀
     */
    fun setupWebChromeClient(context: Context, webViewActivity: WebViewActivity): WebChromeClient {
        return object: WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                InputFileManager.setCallBack(filePathCallback)
                
                //input "File" 대응
                webViewActivity.openInputFile()
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
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            GLog.d("URL 들어옴 2 ${view.url}")
                            return false
                        }
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
                GLog.d("${consoleMessage?.message()}")
                return super.onConsoleMessage(consoleMessage)
            }

            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                GLog.d("웹 js alert 호출")
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
                GLog.d("웹 js Confirm 호출")
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

    //쿠키 초기화
    fun cookieInit(url:String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            // 웹뷰 호스트의 쿠키를 가져옵니다.
            val cookieManager = CookieManager.getInstance()
            cookieManager.getCookie(url)?.let { cookies ->
                // 쿠키 정보를 로그에 출력합니다.
                GLog.d("Webview:cookies = $cookies")
                // 쿠키 문자열을 ";"로 분리하여 배열로 만듭니다.
                val arrCookie = cookies.split(";")
                // 분리된 쿠키 각각에 대해 조건을 검사합니다.
                arrCookie.forEach { cookie ->
                    GLog.d("cookie = $cookie")
                    val cookieData = cookie.split("=")
                    val key = cookieData[0]
                    //쿠키는 다시 설정합니다.
                    if (key=="JSESSIONID") {
                        GLog.d("쿠키 다시 설정: $key=")
                        cookieManager.setCookie(Constants.getWebViewHost(), "$key=")
                    }
                }
            }
        }
    }
}