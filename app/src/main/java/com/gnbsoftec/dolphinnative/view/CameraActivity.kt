package com.gnbsoftec.dolphinnative.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.databinding.ActivityCameraBinding
import com.gnbsoftec.dolphinnative.util.BackPressUtil
import com.gnbsoftec.dolphinnative.util.BitmapUtil
import com.gnbsoftec.dolphinnative.util.Logcat
import com.gnbsoftec.dolphinnative.util.PermissionUtil
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BaseActivity<ActivityCameraBinding>(R.layout.activity_camera) {
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private var savedUri: Uri? = null

    //권한체크 결과처리
    private lateinit var requestPermission : ActivityResultLauncher<Array<String>>
    //권한 콜백 플래그
    private var settingViewFlag = false

    //카메라 이미지 미리보기
    private lateinit var requestPreView : ActivityResultLauncher<Intent>

    //카메라 셔터 버튼 애니메이션
    private lateinit var cameraAnimationListener : Animation.AnimationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        cameraExecutor = Executors.newSingleThreadExecutor()

        val permissionList = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            listOf(Manifest.permission.CAMERA)
        }else{
            listOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionUtil.requestPermission(activity,permissionResult,permissionList){ isGrant->
            if (isGrant) {
                openCamera()
            } else {
//                AlertUtil.showAlert(activity,"안내","카메라 권한을 확인해주세요.","닫기", mOkCallback = {
//                    loading(activity,true,1000){
//                        endCamera()
//                    }
//                })
            }
        }
    }

    override fun initView() {
        //뒤로가기 셋팅
        onBackPressedDispatcher.addCallback(this, BackPressUtil.backBtn(binding,activity))

        //권한체크 콜백
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { permission -> permission.value }) {
                Logcat.d("승인")
                openCamera()
            } else {
                Logcat.d("승인 거부")
//                alertDlg("권한 체크좀해주세요", callback = {
//                    settingViewFlag = true
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    val uri = Uri.fromParts("package", activity.packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
//                })
            }
        }

        //이미지 미리보기
        requestPreView = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result ->
            if(result.resultCode == RESULT_OK){
                result.data?.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                setResult(RESULT_OK,result.data)
                finish()
            }
        }

        //사진 촬영 버튼
        binding.imageViewPhoto.setOnClickListener {
            savePhoto()
        }

        //사진 촬영 버튼 애니메이션
        cameraAnimationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.frameLayoutShutter.visibility= View.GONE

                val intent = Intent(activity,CameraPreviewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    putExtra("imgUrl",savedUri.toString())
                }
                requestPreView.launch(intent)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        }
    }



    private fun openCamera() {
        Logcat.d("openCamera")

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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                Logcat.d("바인딩 성공")

            } catch (e: Exception) {
                Logcat.d("바인딩 실패 $e")
            }
        }, ContextCompat.getMainExecutor(activity))

    }

    @SuppressLint("SimpleDateFormat")
    private fun savePhoto() {
        imageCapture = imageCapture ?: return

//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "gnb_img_" + timeStamp + "_"
//        val photoFile = File(
//            outputDirectory,
//            "$imageFileName.jpg"
//        )

        val photoFile = BitmapUtil.createImageFile()

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

//                    savedUri = Uri.fromFile(photoFile)
                    val resizeFile = BitmapUtil.imgResize(activity,Uri.fromFile(photoFile))
                    photoFile.delete()
                    savedUri = Uri.fromFile(resizeFile)

                    Logcat.d("savedUri : $savedUri")

                    val animation = AnimationUtils.loadAnimation(activity, R.anim.anim_shutter)
                    animation.setAnimationListener(cameraAnimationListener)
                    binding.frameLayoutShutter.animation = animation
                    binding.frameLayoutShutter.visibility = View.VISIBLE
                    binding.frameLayoutShutter.startAnimation(animation)


                    Logcat.d("imageCapture")
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
    }
}