package com.gnbsoftec.dolphinnative.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object PermissionUtil {
    private val contractMulti = ActivityResultContracts.RequestMultiplePermissions()
    private var permissionCallback: ((Boolean) -> Unit?)? = null
    private var permissionList:List<String>? =  null

    fun permissionList():List<String>{
        return ArrayList<String>().apply {
            //휴대폰 상태 권한
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Manifest.permission.READ_PHONE_NUMBERS)
            }

            //푸쉬 권한
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                add(Manifest.permission.POST_NOTIFICATIONS)
            }

            //파일읽기 권한
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            add(Manifest.permission.CAMERA)
        }
    }

    /**
     * 요청 권한
     */
    fun requestPermission(context: Context, permissionResult : ActivityResultLauncher<Array<String>>, list: List<String>, callback: (Boolean) -> Unit){
        permissionCallback=callback
        permissionList = list
        if(checkPermission(context,permissionList!!)){
            permissionCallback!!(true)
        }
        else{
            permissionResult.launch(permissionList!!.toTypedArray())
        }
    }

    /**
     * 응답 권한
     */
    fun responsePermission(activity: ComponentActivity, permissionSettingResult: ActivityResultLauncher<Intent>): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(contractMulti) { resultMap ->
            val isAllGranted = permissionList!!.all { e -> resultMap[e] == true }
            GLog.d("isAllGranted : $isAllGranted")
            if(isAllGranted){
                permissionCallback!!(true)
            }else{
                val packageUri = Uri.fromParts("package",activity.packageName,null)
                AlertUtil.showConfirm(activity, "안내.","권한을 확인해주세요.","설정","취소" , mOkCallback = {
                    try{
                        permissionSettingResult.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        })
                    }catch (e:Exception){
                        ErrorUtil.errorPress(e)
                    }
                }, mCancelCallback = {
                    GLog.d("권한 허용안함")
                })
            }
        }
    }


    fun requestPermission2(activity: ComponentActivity) : ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val list = ArrayList<String>()
            for(per in permissionList!!){
                checkAuth(activity,per,list)
            }
            if(list.size==0){
                permissionCallback!!(true)
            }else {
                permissionCallback!!(false)
            }
        }
    }

    /**
     * 권한 체크 후 없는결과 리스트 적재
     */
    private fun checkAuth(context: Context, permission: String, arr: ArrayList<String>) {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            GLog.d("권한거부 : $permission")
            arr.add(permission)
        }
    }
    /**
     * 권한 체크
     */
    fun checkPermission(context: Context, permissionList: List<String>): Boolean {
        for (i: Int in permissionList.indices) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permissionList[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }
}