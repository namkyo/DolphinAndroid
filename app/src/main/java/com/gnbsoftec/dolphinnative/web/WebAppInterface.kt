package com.gnbsoftec.dolphinnative.web

import androidx.appcompat.app.AppCompatActivity
import com.gnbsoftec.dolphinnative.view.WebViewActivity
import com.gnbsoftec.dolphinnative.web.`interface`.CommonInterface
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * 인터페이스 공통코드 구현
 */
class WebAppInterface(private val activity: AppCompatActivity) : SubInterface , CommonInterface{
    override val webViewActivity:WebViewActivity get() = activity as WebViewActivity

    override val codeSucc0000:String get() = "0000"
    override val codeFail0001:String get() = "0001"
    override val codeMiss0010:String get() = "0010"
    override val codeError0020:String get() = "0020"
    override val codeCancel0030:String get() = "0030"


    override val descSucc:String get() = "정상처리"
    override val descFail:String get() = "처리실패"
    override val descMiss:String get() = "필수값이 입력되지 않았습니다"
    override val descError:String get() = "정의되지 않은 문자열입니다"
    override val descCancel: String get() = "사용자취소"

    override val gson: Gson get() = GsonBuilder().disableHtmlEscaping().create()
}