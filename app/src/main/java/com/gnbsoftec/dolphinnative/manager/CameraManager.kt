package com.gnbsoftec.dolphinnative.manager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.util.DateUtil
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.ImgUtil

object CameraManager {
    private var callback: ((List<Map<String,String>>) -> Unit)? = null

    fun setCallBack(mCallback:(List<Map<String,String>>) -> Unit){
        callback=mCallback
    }

    fun resultCamera(activity: Activity,resultCode: Int, data: Intent?) {
        val list = ArrayList<Map<String,String>>()
        GLog.d("카메라 화면 후 복귀")
        if (resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(Constants.keys.imgKey)?.let {imgUri->
                ImgUtil.resizeImageFromUri(activity.applicationContext,Uri.parse(imgUri),1024)?.let {img->
                    ImgUtil.convertBitmapToBase64(img).let {base64Img->
                        GLog.d("카메라 후 복귀 ${base64Img.length}")
                        list.add(HashMap<String,String>().apply {
                            put("imgName", "gnb_"+ DateUtil.getTimestamp("yyyyMMddHHmmss")+".png")
                            put("imgStr",base64Img)
                        })
                    }
                }
            }
        }
        callback?.let { it(list) }
    }
}