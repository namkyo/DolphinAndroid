package com.gnbsoftec.dolphinnative.manager

import android.app.Activity
import android.content.Intent
import com.gnbsoftec.dolphinnative.util.DateUtil
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.ImgUtil

object PickerManager {
    private var callback: ((List<Map<String,String>>) -> Unit)? = null

    fun setCallBack(mCallback:(List<Map<String,String>>) -> Unit){
        callback=mCallback
    }

    fun resultPicker(activity: Activity,resultCode: Int, data: Intent?) {
        GLog.d("앨범 복귀")
        val list = ArrayList<Map<String,String>>()
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let {imgUri->
                val fileName = if(imgUri.lastPathSegment==null){
                    ("dolphin_${DateUtil.getTimestamp("yyyyMMddHHmmss")}.png")
                }else{
                    imgUri.lastPathSegment!!
                }
                ImgUtil.resizeImageFromUri(activity.applicationContext,imgUri,1024)?.let {img->
                    ImgUtil.convertBitmapToBase64(img).let {base64Img->
                        GLog.d("앨범 후 복귀 $fileName ${base64Img.length}")
                        list.add(HashMap<String,String>().apply {
                            put("imgName",fileName)
                            put("imgStr",base64Img)
                        })
                    }
                }
            }
        }
        callback?.let { it(list) }
    }
}