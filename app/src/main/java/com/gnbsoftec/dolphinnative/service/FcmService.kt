package com.gnbsoftec.dolphinnative.service

import android.text.TextUtils
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.db.DbUtil
import com.gnbsoftec.dolphinnative.extension.parseModel
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.NotificationUtil
import com.gnbsoftec.dolphinnative.util.PreferenceUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class FcmService : FirebaseMessagingService() {
    //이미지 다운로드용 코루틴
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GLog.d("FCM New token: $token")
        //토큰 발급시 내부저장소 저장
        PreferenceUtil.put(this@FcmService.applicationContext, PreferenceUtil.keys.PUSH_KEY,token)
        //푸쉬 수신 허용여부 처음 설치시 Y
        PreferenceUtil.put(this@FcmService.applicationContext, PreferenceUtil.keys.PUSH_YN,"Y")

        FirebaseMessaging.getInstance().subscribeToTopic(Constants.topic)
            .addOnCompleteListener { task ->
                var msg = "Subscription successful"
                if (!task.isSuccessful) {
                    msg = "Subscription failed"
                }
                GLog.d("FCM subscribeToTopic : $msg")
            }
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val pushYn = PreferenceUtil.getValue(this@FcmService.applicationContext, PreferenceUtil.keys.PUSH_YN,"N")
        GLog.d("FCM push수신여부 : $pushYn , remoteMessage: ${remoteMessage.data}")

        val fcmMessage = remoteMessage.data.parseModel(FcmModel.Message::class.java)
        GLog.d("FCM fcmMessage: $fcmMessage")
        
        //푸쉬 수신여부 체크
        if(fcmMessage!=null && "Y"==PreferenceUtil.getValue(this@FcmService.applicationContext, PreferenceUtil.keys.PUSH_YN,"N")){
            DbUtil.insertPushMessage(this@FcmService.applicationContext,fcmMessage)
            // 푸쉬
            NotificationUtil.showNotiService(this@FcmService.applicationContext,serviceScope,fcmMessage)
        }
    }
}