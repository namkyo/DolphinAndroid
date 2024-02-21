package com.gnbsoftec.dolphinnative.common

import android.content.Context
import android.webkit.WebSettings
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.util.GLog

object Constants {
    //운영,개발모드
    const val IS_REAL   =   true

    const val callScript = "gnb"

    const val topic = "GNB"

    const val IS_DEBUG   =   true
    //웹뷰로드시 캐시 삭제대상
    const val jsessionId = "JSESSIONID="

    //웹뷰 캐시 모드
    val webViewCacheMode = if(IS_REAL){
        WebSettings.LOAD_DEFAULT
    }else{
        WebSettings.LOAD_NO_CACHE
    }


    // 메인웹뷰 URL
    fun getWebViewHost() : String {
        return if (IS_REAL) {
            BuildConfig.prodUrl
        }else{
            BuildConfig.devUrl
        }
    }

    /**
     * 폴드 펼친화면 여부
     */
    fun isFold(context: Context): Boolean {
        val display = context.resources?.displayMetrics
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

    object keys {
        const val imgKey = "imgKey"
    }
}