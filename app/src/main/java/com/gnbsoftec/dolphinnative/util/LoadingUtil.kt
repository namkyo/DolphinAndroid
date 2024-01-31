package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.gnbsoftec.dolphinnative.databinding.DialogLoadingBinding

object LoadingUtil {

    private var dialog: Dialog? = null

    fun isShow(): Boolean {
        return dialog?.isShowing ?: false
    }

    fun showLoading(activity: Activity) {
        activity.runOnUiThread {
            if (dialog == null) {
                val binding = DialogLoadingBinding.inflate(LayoutInflater.from(activity))
                dialog = AlertDialog.Builder(activity)
                    .setView(binding.root)
                    .setCancelable(false)
                    .create()
                dialog?.window?.apply {
                    // 배경색을 검정색으로 설정하고 투명도(알파)를 0.4로 설정
//                    setBackgroundDrawable(ColorDrawable(Color.argb((0.4f * 255).toInt(), 0, 0, 0)))
                     setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                (binding.loadingAnim.drawable as? AnimationDrawable)?.start()
            }

            dialog?.show()
        }
    }

    fun hideLoading() {
        dialog?.dismiss()
    }
}