package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import com.gnbsoftec.dolphinnative.databinding.ActivityMainBinding

class onBackPressedUtil private constructor(var binding: ActivityMainBinding)  {
    companion object {
        private var instance: onBackPressedUtil? = null
        fun getInstance(binding: ActivityMainBinding,activity: Activity): onBackPressedUtil {
            return instance ?: synchronized(this) {
                instance ?: onBackPressedUtil(binding).also {
                    instance = it
                }
            }
        }
    }
}