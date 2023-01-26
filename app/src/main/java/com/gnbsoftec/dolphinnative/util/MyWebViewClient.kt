package com.gnbsoftec.dolphinnative.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gnbsoftec.dolphinnative.databinding.ActivityMainBinding

class MyWebViewClient private constructor(val binding: ActivityMainBinding) : WebViewClient() {
    companion object {
        private var instance: MyWebViewClient? = null
        fun getInstance(binding: ActivityMainBinding): MyWebViewClient {
            return instance ?: synchronized(this) {
                instance ?: MyWebViewClient(binding).also {
                    instance = it
                }
            }
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        binding.splashView.animate()
            .alpha(0.0f)
            .setDuration(600)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.splashView.visibility = View.GONE
                }
            })
        super.onPageStarted(view, url, favicon)
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
    }
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }
}