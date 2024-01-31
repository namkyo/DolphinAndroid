package com.gnbsoftec.dolphinnative.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.activity.MainActivity
import com.gnbsoftec.dolphinnative.config.Constants
import com.gnbsoftec.dolphinnative.util.BitmapUtil
import com.gnbsoftec.dolphinnative.util.Logcat
import com.gnbsoftec.dolphinnative.util.SharedPreferenceHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService: FirebaseMessagingService() {
    private val sp : SharedPreferenceHelper = SharedPreferenceHelper(this)

    // 토큰 생성
    override fun onNewToken(token: String) {
        sp.put("token",token)
        Logcat.d("onNewToken token: $token")
        FirebaseMessaging.getInstance().subscribeToTopic("ALL").addOnCompleteListener { tast->
            if(tast.isSuccessful){
                Logcat.d("구독완료")
            }else{
                Logcat.d("구독실패")
            }
        }
    }


    // 메시지 수신
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Logcat.d("From: $remoteMessage.notification")

        val message = remoteMessage.data
        if(message.isEmpty()){
            Logcat.d("수신에러 : data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Logcat.d("data값 :${remoteMessage.data}")
        }else{
            sendNotification(message)
        }
    }


    // 알림 생성 (아이콘, 알림 소리 등)
    private fun sendNotification(message: MutableMap<String, String>){
        // RemoteCode, ID를 고유값으로 지정하여 알림이 개별 표시 되도록 함
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent
        // PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack을 경로만 남김, A-B-C-D-B => A-B

        message[Constants.pushIntentData.pushUrl].let {
            intent.putExtra(Constants.pushIntentData.pushUrl,it)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        // 알림 채널 이름
        val channelId = getString(R.string.firebase_notification_channel_id)

        // 알림 소리
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림에 대한 UI 정보와 작업을 지정
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)     // 아이콘 설정
            .setAutoCancel(true)
            .setSound(soundUri)     // 알림 소리
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)       // 알림 실행 시 Intent
            .setContentTitle(message[Constants.pushIntentData.title])
            .setContentText(message[Constants.pushIntentData.body])

        Logcat.d("message : $message")

        if(TextUtils.isEmpty(message[Constants.pushIntentData.image])){
            val textStyle = NotificationCompat.BigTextStyle()
            textStyle.setBigContentTitle(message[Constants.pushIntentData.title])
            textStyle.bigText(message[Constants.pushIntentData.body])
            notificationBuilder.setStyle(textStyle)
        }else{
            val img = BitmapUtil.urlToBitmap("${message[Constants.pushIntentData.image]}")
            //작은 이미지 아이콘
            notificationBuilder.setLargeIcon(img)

            val pictureStyle = NotificationCompat.BigPictureStyle()
            //상세보기 이미지 아이콘
            pictureStyle.bigPicture(img)
//            pictureStyle.bigLargeIcon(null)
            pictureStyle.setBigContentTitle(message[Constants.pushIntentData.title])
            pictureStyle.setSummaryText(message[Constants.pushIntentData.body])
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                pictureStyle.showBigPictureWhenCollapsed(true)
//            }
            notificationBuilder.setStyle(pictureStyle)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())
    }
}