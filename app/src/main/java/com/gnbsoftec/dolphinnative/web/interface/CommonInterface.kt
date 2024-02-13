package com.gallery.orix.web.`interface`

import android.webkit.JavascriptInterface
import com.gallery.orix.BuildConfig
import com.gallery.orix.common.Constants
import com.gallery.orix.manager.CameraManager
import com.gallery.orix.manager.PickerManager
import com.gallery.orix.util.FileUtil
import com.gallery.orix.util.GLog
import com.gallery.orix.util.LoadingUtil
import com.gallery.orix.util.NotificationUtil
import com.gallery.orix.util.PermissionUtil
import com.gallery.orix.util.PreferenceUtil
import com.gallery.orix.util.TelUtil
import com.gallery.orix.util.ToastUtil
import com.gallery.orix.web.SubInterface
import com.gallery.orix.web.model.InterfaceModel

interface CommonInterface : SubInterface {
    /**
     * [1] : 기기정보 조회
     */
    @JavascriptInterface
    fun getDeviceInfo(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InGetDeviceInfo::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {
            val context = webViewActivity.applicationContext
            val simState = TelUtil.getSimState(context)
            val networkOperator = TelUtil.getNetworkOperator(context)
            val phoneNumber = try{
                TelUtil.getPhoneNumber(context)
            }catch (e:Exception){
                GLog.e("휴대폰번호 가져오기 오류")
                ""
            }

            // 3 - 데이터 처리
            val info = mapOf(
                "APPLICATION_ID" to BuildConfig.APPLICATION_ID,
                "BUILD_TYPE" to BuildConfig.BUILD_TYPE,
                "VERSION_CODE" to BuildConfig.VERSION_CODE.toString(),
                "VERSION_NAME" to BuildConfig.VERSION_NAME,
                "SIM_STATE" to simState,
                "NET_OPERATOR" to networkOperator,
                "PHONE_NUMBER" to phoneNumber,
                "PUSH_KEY" to PreferenceUtil.getValue(context,PreferenceUtil.keys.PUSH_KEY,"")
            )

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutGetDeviceInfo(codeSucc0000, descSucc, info)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }



    @JavascriptInterface
    fun loading(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InLoading::class.java)

        // 2 - 정상 파싱
        if (inParam?.flag != null) {

            // 3 - 데이터 처리
            val outParam = try {
                if(inParam.flag=="Y"){
                    if(!LoadingUtil.isShow()){
                        LoadingUtil.showLoading(webViewActivity)
                    }
                }else{
                    if(LoadingUtil.isShow()){
                        LoadingUtil.hideLoading()
                    }
                }
                InterfaceModel.OutLoading(codeSucc0000,descSucc)
            }catch (e:Exception){
                GLog.e("로딩바 에러",e)
                InterfaceModel.OutLoading(codeFail0001,descFail)
            }

            // 4 - 결과 전송
            callbackScript(inParam.callback,inParam.cmd,gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    /**
     * 샘플
     */
    @JavascriptInterface
    fun appClose(jsonString: String) {
        webViewActivity.appClose()
    }



    @JavascriptInterface
    fun setLocalStorage(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InSetLocalStorage::class.java)

        // 2 - 정상 파싱
        if (inParam?.data != null) {

            // 3 - 데이터 처리
            for(data in inParam.data){
                GLog.d("저장소 적재 키:${data.key} , 값:${data.value}")
                PreferenceUtil.put(webViewActivity.applicationContext,data.key,data.value)
            }

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutSetLocalStorage(codeSucc0000,descSucc)
            callbackScript(inParam.callback,inParam.cmd,gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }
    @JavascriptInterface
    fun getLocalStorage(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InGetLocalStorage::class.java)

        // 2 - 정상 파싱
        if (inParam?.keys != null) {

            val data = HashMap<String,String>()
            // 3 - 데이터 처리
            for(key in inParam.keys){
                val value = PreferenceUtil.getValue(webViewActivity.applicationContext,key,"")
                GLog.d("저장소 조회 키:$key , 값:$value")
                data[key] = value
            }

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutGetLocalStorage(codeSucc0000,descSucc, data)
            callbackScript(inParam.callback,inParam.cmd,gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun getGalleryImage(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InGetGalleryImage::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            PickerManager.setCallBack { imgList->

                // 4 - 결과 전송
                val outParam = InterfaceModel.OutGetGalleryImage(codeSucc0000, descSucc, imgList)
                callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
            }

            webViewActivity.openGallery()

        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }


    @JavascriptInterface
    fun getCameraImage(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InGetCameraImage::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {


            CameraManager.setCallBack { imgList->

                // 4 - 결과 전송
                val outParam = InterfaceModel.OutGetCameraImage(codeSucc0000, descSucc, imgList)
                callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
            }

            webViewActivity.openCamera()

        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun urlFileDownload(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InUrlFileDownload::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            //상대경로 처리
            val fileUrl = if(inParam.fileUrl.startsWith("http")){
                inParam.fileUrl
            }else{
                Constants.getWebViewHost()+inParam.fileUrl
            }

            // 3 - 데이터 처리
            FileUtil.urlFileDownload(webViewActivity,fileUrl){result,msg->
                GLog.d("다운로드 결과 result=$result , msg=$msg")
                // 4 - 결과 전송
                val outParam = if(result){
                    InterfaceModel.OutUrlFileDownload(codeSucc0000, descSucc)
                }else{
                    InterfaceModel.OutUrlFileDownload(codeFail0001, descFail)
                }
                callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
            }

        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }


    @JavascriptInterface
    fun base64FileDownload(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InBase64FileDownload::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            FileUtil.base64FileDownload(webViewActivity,inParam.fileName,inParam.base64str){result,msg->
                GLog.d("다운로드 결과 result=$result , msg=$msg")
                // 4 - 결과 전송
                val outParam = if(result){
                    InterfaceModel.OutBase64FileDownload(codeSucc0000, descSucc)
                }else{
                    InterfaceModel.OutBase64FileDownload(codeFail0001, descFail)
                }
                callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
            }
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun permissionSelect(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InPermissionSelect::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            val checkYn = if(PermissionUtil.checkPermission(webViewActivity.applicationContext,PermissionUtil.permissionList())){
                "Y"
            }else{
                "N"
            }

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutPermissionSelect(codeSucc0000, descSucc , checkYn)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun permissionCheck(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InPermissionCheck::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            webViewActivity.mainPermission(PermissionUtil.permissionList()){
                val checkYn = if(PermissionUtil.checkPermission(webViewActivity.applicationContext,PermissionUtil.permissionList())){
                    "Y"
                }else{
                    "N"
                }
                // 4 - 결과 전송
                val outParam = InterfaceModel.OutPermissionCheck(codeSucc0000, descSucc , checkYn)
                callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
            }

        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun showToast(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InShowToast::class.java)

        // 2 - 정상 파싱
        if (inParam?.msg != null) {

            // 3 - 데이터 처리
            ToastUtil.show(webViewActivity,inParam.msg,3000)

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutShowToast(codeSucc0000, descSucc)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun showNotification(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InShowNotification::class.java)

        // 2 - 정상 파싱
        if (inParam?.msg != null) {

            // 3 - 데이터 처리
            NotificationUtil.showTextNotification(webViewActivity.applicationContext,inParam.title,inParam.msg)
            // 4 - 결과 전송
            val outParam = InterfaceModel.OutShowToast(codeSucc0000, descSucc)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    
    /**
     * 샘플
     */
    @JavascriptInterface
    fun customFunc(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InGetDeviceInfo::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            val info = mapOf(
                "APPLICATION_ID" to BuildConfig.APPLICATION_ID
            )

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutGetDeviceInfo(codeSucc0000, descSucc, info)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }
}