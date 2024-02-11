package com.gnbsoftec.dolphinnative.util

import android.content.Context
import android.text.TextUtils
import com.gnbsoftec.dolphinnative.view.CustomAlert

object AlertUtil {
    /**
     * 커스텀 bottom alert
     */
    fun showAlert(context: Context, title:String, message:String, okBtnStr:String,
                  mOkCallback:((Boolean) -> Unit)){
        if(TextUtils.isEmpty(message)){return}
        GLog.d("showAlert : $message")
        CustomAlert(context ,title ,message, okBtnStr, "" ,mOkCallback , null).show()
    }
    /**
     * 커스텀  bottom  confirm
     */
    fun showConfirm(context: Context, title:String, message:String, okBtnStr:String, cancelBtnStr:String,
                    mOkCallback:((Boolean) -> Unit),
                    mCancelCallback:((Boolean) -> Unit)){
        if(TextUtils.isEmpty(message)){return}
        GLog.d("showAlert : $message")
        CustomAlert(context ,title ,message, okBtnStr, cancelBtnStr ,mOkCallback , mCancelCallback).show()
    }
}