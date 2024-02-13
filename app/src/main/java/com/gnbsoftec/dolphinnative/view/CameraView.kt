package com.gnbsoftec.dolphinnative.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.databinding.ActivityCameraViewBinding
import com.gnbsoftec.dolphinnative.util.AlertUtil
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.ImgUtil
import com.gnbsoftec.dolphinnative.util.PermissionUtil
import java.io.File
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraView : BaseActivity<ActivityCameraViewBinding>(R.layout.activity_camera_view) {

    //카메라 보이는 뷰
    private var imageCapture: ImageCapture? = null

    //카메라X 모듈
    private lateinit var cameraExecutor: ExecutorService

    //카메라 셔터음
    private lateinit var shutterSound : MediaActionSound

    //카메라 결과 화면
    private lateinit var cameraPreViewLauncher: ActivityResultLauncher<Intent>
    override fun initView() {

        cameraExecutor = Executors.newSingleThreadExecutor()
        imageCapture = ImageCapture.Builder().build()

        shutterSound = MediaActionSound()

        onBackPressedDispatcher.addCallback(this@CameraView, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                endCamera()
            }
        })

        val permissionList = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            listOf(Manifest.permission.CAMERA)
        }else{
            listOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        loading(activity,true,1000){
            PermissionUtil.requestPermission(activity,permissionResult,permissionList){ isGrant->
                if (isGrant) {
                    openCamera()
                } else {
                    AlertUtil.showAlert(activity,"안내","카메라 권한을 확인해주세요.","닫기", mOkCallback = {
                        loading(activity,true,1000){
                            endCamera()
                        }
                    })
                }
            }
        }

        binding.btClose.setOnClickListener {
            GLog.d("btShot.btClose")
            endCamera()
        }
        binding.btShot.setOnClickListener {
            GLog.d("btShot.savePhoto")
            savePhoto()
        }

        cameraPreViewLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result ->
            GLog.d("카메라 화면 돌아옴 ${result.resultCode}")
            if(result.resultCode == RESULT_OK && result.data != null){
                GLog.d("카메라 촬영 화면 종료 ${result.resultCode == RESULT_OK} ${result.data!!.getStringExtra(
                    Constants.keys.imgKey)}")
                setResult(RESULT_OK,Intent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    putExtra(Constants.keys.imgKey,result.data!!.getStringExtra(Constants.keys.imgKey))
                })
                finish()
            }
        }
    }

    private fun openCamera() {
        GLog.d("openCamera")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@CameraView, cameraSelector, preview, imageCapture)
                GLog.d("바인딩 성공")

            } catch (e: Exception) {
                GLog.d("바인딩 실패 $e")
            }
        }, ContextCompat.getMainExecutor(activity))

    }

    private fun savePhoto() {
        //찰칵음 재생
        shutterSound.play(MediaActionSound.SHUTTER_CLICK)

        imageCapture = imageCapture ?: return

        val fileName = SimpleDateFormat("yy-mm-dd", Locale.KOREA).format(System.currentTimeMillis()) + ".png"
        val photoFile = File.createTempFile(fileName,null,activity.cacheDir)
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cameraExecutor.shutdown()

                    val imgUri = Uri.fromFile(ImgUtil.resizeImageFile(photoFile,1024))
                    GLog.d("imgKey : $imgUri")

                    val animation = AnimationUtils.loadAnimation(activity, R.anim.anim_shutter)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                        }
                        override fun onAnimationEnd(animation: Animation?) {
                            val intent = Intent(activity,CameraPreView::class.java)
                            intent.putExtra(Constants.keys.imgKey,imgUri.toString())
                            cameraPreViewLauncher.launch(intent)
                        }
                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    binding.btShot.animation = animation
                    binding.btShot.visibility = View.VISIBLE
                    binding.btShot.startAnimation(animation)
                    GLog.d("imageCapture")
                }
                override fun onError(e: ImageCaptureException) {
                    e.printStackTrace()
                    endCamera()
                }
            })
    }

    private fun endCamera(){
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("image","11")
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}