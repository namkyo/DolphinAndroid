package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.system.exitProcess

object BackPressUtil {
    //뒤로가기 연속 클릭 대기 시간
    private var mBackWait: Long = 0

    fun webView(webView: WebView,binding : ViewBinding): OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 클릭 시 실행시킬 코드 입력
                if (webView.canGoBack()) {
                    webView.goBack()
                    GLog.d("뒤로가기 클릭1")
                } else {
                    if (System.currentTimeMillis() - mBackWait >= 2000) {
                        mBackWait = System.currentTimeMillis()
                        Snackbar.make(
                            binding.root,
                            "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        mBackWait=0
                        exitProcess(0)
                    }
                    GLog.d("뒤로가기 클릭2")
                }
            }
        }

    fun backBtn(binding : ViewBinding,activity:Activity): OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 클릭 시 실행시킬 코드 입력
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Snackbar.make(
                        binding.root,
                        "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    mBackWait=0
                    activity.finish() //액티비티 종료
//                    exitProcess(0)
                }
                GLog.d("뒤로가기 클릭")
            }
        }

    fun backBtn(binding : ViewBinding): OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 클릭 시 실행시킬 코드 입력
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Snackbar.make(
                        binding.root,
                        "뒤로가기 버튼을 한번 더 누르면 종료됩니다.",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    mBackWait=0
                    exitProcess(0)
                }
                GLog.d("뒤로가기 클릭")
            }
        }

}