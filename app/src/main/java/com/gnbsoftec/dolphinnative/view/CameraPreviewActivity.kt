package com.gnbsoftec.dolphinnative.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.databinding.ActivityCameraPreviewBinding
import com.gnbsoftec.dolphinnative.util.BackPressUtil

class CameraPreviewActivity : BaseActivity<ActivityCameraPreviewBinding>(R.layout.activity_camera_preview){
    // 제스처 이벤트 감지하는 변수
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initView(){
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

        mScaleGestureDetector = ScaleGestureDetector(activity, ScaleListener())
    }
    // 제스처 이벤트가 발생하면 실행되는 메소드
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        // 제스처 이벤트를 처리하는 메소드를 호출
        mScaleGestureDetector.onTouchEvent(motionEvent)
        return true
    }

    // 제스처 이벤트를 처리하는 클래스
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {

            scaleFactor *= scaleGestureDetector.scaleFactor

            // 최소 0.5, 최대 2배
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 6.0f))

            // 이미지에 적용
            binding.imageViewPhoto.scaleX = scaleFactor
            binding.imageViewPhoto.scaleY = scaleFactor
            return true
        }
    }
}