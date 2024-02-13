package com.gnbsoftec.dolphinnative.util

import android.webkit.WebView
import androidx.activity.OnBackPressedCallback

object BackPressUtil {

    /**
     * 웹뷰 뒤로가기 정책
     */
    fun webView(webView: WebView, backScript: String): OnBackPressedCallback {
        return  object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //웹뷰 버튼 정의
                webView.post {
                    webView.loadUrl(backScript)
                }
            }
        }
    }
}