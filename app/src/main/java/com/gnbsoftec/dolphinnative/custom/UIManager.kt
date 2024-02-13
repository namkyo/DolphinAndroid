package com.gnbsoftec.dolphinnative.custom

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.view.CustomAlert

object UIManager {
    /**
     * 커스텀 alert
     */
    fun showAlert(context: Context, message:String,
                  mOkCallback:((Boolean) -> Unit)){
        if(TextUtils.isEmpty(message)){return}
        GLog.d("showAlert : $message")
        CustomAlert(context ,"안내",message,"확인","취소",mOkCallback , null).show()
    }

    /**
     * 커스텀 alert 후 앱 종료
     */
    fun showAlertFinish(activity: Activity, message:String){
        if(TextUtils.isEmpty(message)){return}
        GLog.d("showAlert : $message")
//        CustomAlert(activity ,message, okCallback = {
//            BackPressUtil.appClose(activity)
//        } , null).show()
    }
}