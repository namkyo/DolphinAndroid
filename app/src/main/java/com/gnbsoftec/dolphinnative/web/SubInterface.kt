package com.gallery.orix.web

import com.gallery.orix.extension.toJsonString
import com.gallery.orix.util.GLog
import com.gallery.orix.view.WebViewActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * 인터페이스 공통코드 정의
 */
interface SubInterface {
    val webViewActivity: WebViewActivity

    val gson : Gson

    val codeSucc0000:String
    val codeFail0001:String
    val codeMiss0010:String
    val codeError0020:String
    val codeCancel0030:String

    val descSucc:String
    val descFail:String
    val descMiss:String
    val descError:String
    val descCancel:String


    fun <T> parseJson(jsonString: String, clazz: Class<T>): T? {
        GLog.d("네이티브 통신 인입 파싱 ${clazz.name} , jsonString 데이터 : $jsonString")
        return try {
            Gson().fromJson(jsonString, clazz)
        } catch (e: JsonSyntaxException) {
            GLog.e("Error parsing JSON", e)
            null // 오류 발생 시 null 반환
        }
    }

    /**
     * 정상 콜백
     */
    fun callbackScript(callback: String, cmd: String, json: String) {
        GLog.d("네이티브 통신 결과 callback : $callback , cmd : $cmd")
        val callScript = "javascript:$callback['$cmd'](JSON.parse(JSON.stringify(${json.toJsonString()})));"
        GLog.d("callbackScript : $callScript")
        webViewActivity.loadURL(callScript)
    }

    /**
     * 에러 콜백
     */
    fun errorScript(msg:String){
        GLog.d("errorScript msg : $msg")
        webViewActivity.loadURL("javascript:NativeUtil.error('$msg');")
    }
}