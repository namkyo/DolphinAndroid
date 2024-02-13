package com.gnbsoftec.dolphinnative.util

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager

object TelUtil {
    @SuppressLint("MissingPermission", "HardwareIds")
    @Suppress("DEPRECATION")
    fun getPhoneNumber(context: Context):String{
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.line1Number  // 휴대폰 번호]
    }
    fun getSimState(context: Context):String{
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return "${tm.simState}"  // 휴대폰 번호
    }

    fun getNetworkOperator(context: Context):String{
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperatorName  // 휴대폰 번호
    }
}