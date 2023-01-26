package com.gnbsoftec.dolphinnative.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.gnbsoftec.dolphinnative.databinding.ActivityIntroBinding
import com.gnbsoftec.dolphinnative.util.*

class IntroActivity : BaseActivity() {
    //Data 바인딩
    private lateinit var binding : ActivityIntroBinding
    //컨테이너
    private lateinit var activity: Activity
    // 저장소
    private lateinit var sp : SharedPreferenceHelper

    //권한체크 결과처리
    private lateinit var requestPermission : ActivityResultLauncher<Array<String>>

    //권한 콜백 플래그
    private var settingViewFlag = false

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        activity = this
        sp = SharedPreferenceHelper(applicationContext)
        //뷰 바인딩
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //권한체크 콜백
        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            Logcat.d("RequestMultiplePermissions")
            val isGranted = permissions.all {permission ->
                Logcat.d("permission.key  : ${permission.key }")
                Logcat.d("permission.value  : ${permission.value }")
                permission.value
            }
            Logcat.d("isGranted : $isGranted")
            val isStoraged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
            Logcat.d("isStoraged : $isStoraged")

            if(isGranted){
                Logcat.d("승인")
                startActivity()
            }else{
                Logcat.d("승인 거부")
                alertDlg("권한 체크좀해주세요", callback = {
                    settingViewFlag = true
                    if(isStoraged){
                        startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            addCategory("android.intent.category.DEFAULT")
                            data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                        })
                    }else{
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        })
                    }
                })
            }
        }


        if(settingViewFlag == false){
            permissionCheck()
        }
    }

    override fun onResume() {
        if(settingViewFlag){
            settingViewFlag=false
            showToast("권한 재 체크중..")
            permissionCheck()
//            startActivity()
        }
        super.onResume()
    }

    private fun permissionCheck(){

        val networkStatus = NetworkStatus.getConnectivityStatus(activity)

        val permissionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Logcat.d("permissionList1")
            listOf(Manifest.permission.CAMERA)
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Logcat.d("permissionList2")
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.CAMERA)
        }else{
            Logcat.d("permissionList3")
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.CAMERA)
        }


        val isStoraged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
        Logcat.d("isStoraged : $isStoraged")

        if(!isStoraged && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            alertDlg("권한 체크좀해주세요", callback = {
                settingViewFlag = true
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    addCategory("android.intent.category.DEFAULT")
                    data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                })
            })
        }else{
            if(NETWORK_STATUS.TYPE_NOT_CONNECTED == networkStatus){
                alertDlgFinish("네트워크 연결을 확인해주세요")
            }else{
                binding.splashView.alpha = 0f
                binding.splashView.animate()
                    .alpha(1f)
                    .setDuration(1500)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            //실행할 코드
                            if (!PermissionUtil.checkPermission(activity, permissionList)) {
                                Logcat.d("퍼미션 권한X")
                                requestPermission.launch(permissionList.toTypedArray())
                            } else {
                                Logcat.d("퍼미션 권한O")
                                startActivity()
                            }
                        }
                    }).start()
            }
        }


    }

    private fun startActivity(){
        val intent = Intent(activity,MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
        finish()
    }
}