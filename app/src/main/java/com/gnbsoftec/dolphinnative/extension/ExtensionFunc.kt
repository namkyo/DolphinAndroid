package com.gnbsoftec.dolphinnative.extension

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.gnbsoftec.dolphinnative.util.GLog
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.net.URLDecoder


fun String.toJsonString():String{
    return this.replace("\\\"", "\"") // 불필요한 역슬래시 제거
        .replace("\"{","{")
        .replace("}\"","}")
        .replace("\"[","[")
        .replace("]\"","]")
}
fun <T> Map<String, Any>.parseModel(clazz: Class<T>): T? {
    GLog.d("네이티브 통신 인입 파싱 ${clazz.name} , jsonString 데이터 : $this")
    return try {
        // Map 인스턴스를 JSON 문자열로 변환
        val json = Gson().toJson(this)
        // JSON 문자열을 원하는 클래스 타입의 인스턴스로 변환
        Gson().fromJson(json, clazz)
    } catch (e: JsonSyntaxException) {
        GLog.e("Error parsing JSON", e)
        null // 오류 발생 시 null 반환
    }
}

fun String.decodeUrl(): String {
    // "UTF-8"은 URL 디코딩에 사용할 문자 인코딩입니다.
    // URL 인코딩된 문자열을 디코딩하여 반환합니다.
    return URLDecoder.decode(this, "UTF-8")
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

//폴드 펼침여
fun Context.isFold(): Boolean {
    val display = this.resources?.displayMetrics
    return if(display == null) {
        GLog.d("디바이스 정보 조회 에러")
        false
    }else {
        // 태블릿, 폴드 펼침
        if(display.widthPixels > 1600) {
            GLog.d("태블릿, 폴드 펼침")
            true
        }
        // 미니, 폴드 닫힘
        else if(display.widthPixels < 980) {
            GLog.d("미니, 폴드 닫힘")
            false
        }
        // 일반
        else{
            GLog.d("일반 폰")
            false
        }
    }
}

fun Context.getDeviceWidth(): Int {
    val metrics = DisplayMetrics()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11 (API 레벨 30) 이상에서 WindowMetrics 사용
        val windowMetrics = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = windowMetrics.currentWindowMetrics.bounds
        return bounds.width()
    } else {
        // Android 11 미만에서는 기존 방법 사용
        @Suppress("DEPRECATION")
        val display = (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        @Suppress("DEPRECATION")
        display.getMetrics(metrics)
        return metrics.widthPixels
    }
}