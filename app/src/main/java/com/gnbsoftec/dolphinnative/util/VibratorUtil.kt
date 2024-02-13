package com.gnbsoftec.dolphinnative.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object VibratorUtil {
    @SuppressLint("WrongConstant")
    fun vibrator(context: Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            //31 이상
            val vm: VibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vm.defaultVibrator.vibrate(effect)
        }else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            //26~31
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            v.vibrate(effect)
        }else{
            //~25
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(50)
        }
    }
}