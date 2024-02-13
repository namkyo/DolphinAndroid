package com.gnbsoftec.dolphinnative.manager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback

object InputFileManager {
    private var filePathCallback: (ValueCallback<Array<Uri>>)? = null

    fun setCallBack(_filePathCallback: ValueCallback<Array<Uri>>?){
        filePathCallback=_filePathCallback
    }

    fun resultPInputFile(activity: Activity,resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            if (selectedFileUri != null) {
                // 파일 URI를 filePathCallback에 전달합니다.
                filePathCallback?.onReceiveValue(arrayOf(selectedFileUri))
            } else {
                // 파일을 선택하지 않았거나 오류가 발생한 경우
                filePathCallback?.onReceiveValue(null)
            }
        }
    }
}