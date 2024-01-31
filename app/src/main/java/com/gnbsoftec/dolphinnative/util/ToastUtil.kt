package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.gnbsoftec.dolphinnative.databinding.CustomToastLayoutBinding
import com.google.android.material.snackbar.Snackbar

object ToastUtil {
    fun show(activity: Activity, message: String, duration: Int) {
        // activity의 루트 뷰를 사용하여 Snackbar 생성
        val rootView = activity.findViewById<View>(android.R.id.content)
        val snackView = Snackbar.make(rootView, "", duration)

        // 뷰 바인딩을 사용하여 customSnackView 인플레이트
        val binding = CustomToastLayoutBinding.inflate(activity.layoutInflater)
        binding.customToastText.text = message
        binding.customToastText.contentDescription = message

        binding.toastLayout.setOnClickListener {
            snackView.dismiss()
        }

        val snackbarLayout = snackView.view as Snackbar.SnackbarLayout
        snackbarLayout.addView(binding.root, 1)


        val params = snackbarLayout.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin += 110 // 하단 여백을 330px 증가
        snackbarLayout.layoutParams = params

        // 스낵바 배경을 투명하게 설정
        snackbarLayout.setBackgroundColor(Color.TRANSPARENT)

        snackView.show()
    }

}