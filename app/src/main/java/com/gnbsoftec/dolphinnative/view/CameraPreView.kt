package com.gnbsoftec.dolphinnative.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.databinding.ActivityCameraPreviewBinding
import com.gnbsoftec.dolphinnative.util.BackPressUtil

class CameraPreView : BaseActivity<ActivityCameraPreviewBinding>(R.layout.activity_camera_preview) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        onBackPressedDispatcher.addCallback(this@CameraPreView, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                endCamera()
            }
        })

        binding.previewImgView.setImageURI(Uri.parse(intent.getStringExtra(Constants.keys.imgKey)))

        binding.prev.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.next.setOnClickListener{
            setResult(RESULT_OK,Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra(Constants.keys.imgKey,intent.getStringExtra(Constants.keys.imgKey))
            })
            finish()
        }
    }


    private fun endCamera(){
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("image","11")
        })
        finish()
    }
}