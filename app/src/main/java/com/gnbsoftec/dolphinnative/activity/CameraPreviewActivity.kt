package com.gnbsoftec.dolphinnative.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.gnbsoftec.dolphinnative.databinding.ActivityCameraPreviewBinding
import com.gnbsoftec.dolphinnative.util.BackPressUtil

class CameraPreviewActivity : BaseActivity(){
    //Data 바인딩
    private lateinit var binding : ActivityCameraPreviewBinding
    //컨테이너
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this//뷰 바인딩
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init();
    }

    private fun init(){
        //뒤로가기 셋팅
        onBackPressedDispatcher.addCallback(this, BackPressUtil.backBtn(binding,activity))

        val imgUrl = Uri.parse(intent.getStringExtra("imgUrl"))
        binding.imageViewPhoto.setImageURI(imgUrl)

        binding.prev.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.next.setOnClickListener{
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            setResult(RESULT_OK,intent)
            finish()
        }
    }
}