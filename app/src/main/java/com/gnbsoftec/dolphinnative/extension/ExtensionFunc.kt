package com.gnbsoftec.dolphinnative.extension

import com.gnbsoftec.dolphinnative.util.GLog
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException


fun String.toJsonString():String{
    return this.replace("\\\"", "\"") // 불필요한 역슬래시 제거
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