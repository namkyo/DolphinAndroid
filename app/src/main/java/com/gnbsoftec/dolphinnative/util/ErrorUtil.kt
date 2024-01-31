package com.gnbsoftec.dolphinnative.util

import com.gnbsoftec.dolphinnative.common.Constants

object ErrorUtil {
    fun errorPress(e:Exception){
        if(Constants.IS_REAL){
            Glog.e("문제가 발생하였습니다. [${e.localizedMessage}] [${e.message}]")
        }else{
            e.printStackTrace()
        }
    }
}