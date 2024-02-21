package com.gnbsoftec.dolphinnative.web.`interface`

import android.webkit.JavascriptInterface
import com.gnbsoftec.dolphinnative.db.DbUtil
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.web.SubInterface
import com.gnbsoftec.dolphinnative.web.model.InterfaceModel
import com.google.firebase.messaging.FirebaseMessaging

interface PushInterface : SubInterface {
    @JavascriptInterface
    fun pushList(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InPushList::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            val list = DbUtil.getAllPushMessages(webViewActivity.applicationContext)

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutPushList(codeSucc0000, descSucc, list)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    @JavascriptInterface
    fun pushDelete(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InPushDelete::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            val cnt = DbUtil.deleteAllPushMessages(webViewActivity.applicationContext)

            // 4 - 결과 전송
            val outParam = InterfaceModel.OutPushDelete(codeSucc0000, descSucc, cnt)
            callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }

    fun pushTopicUpdate(jsonString: String) {
        // 1 - 데이터 파싱
        val inParam = parseJson(jsonString, InterfaceModel.InPushTopicUpdate::class.java)

        // 2 - 정상 파싱
        if (inParam?.callback != null) {

            // 3 - 데이터 처리
            FirebaseMessaging.getInstance().subscribeToTopic(inParam.topic)
                .addOnCompleteListener { task ->

                    // 4 - 결과 전송
                    val outParam = if(task.isSuccessful){
                        GLog.d("Subscribed to topic successfully")
                        InterfaceModel.OutPushTopicUpdate(codeSucc0000, descSucc,"Y","구독완료")
                    }else{
                        GLog.e("Subscription to topic failed : ${task.exception}")
                        InterfaceModel.OutPushTopicUpdate(codeError0020, descError,"Y","구독에러 : ${task.exception?.message}")
                    }
                    callbackScript(inParam.callback, inParam.cmd, gson.toJson(outParam))
                }

        } else {
            // 5 - 파싱 데이터 불일치
            GLog.e("파싱 불일치")
            errorScript(descMiss)
        }
    }
}