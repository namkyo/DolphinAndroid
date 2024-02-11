package com.gnbsoftec.dolphinnative.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationUtil {
    private val channelId = "channel_orix"
    private val channelName = "orix"
    private val groupKey  = BuildConfig.APPLICATION_ID+".NOTIFICATION_GROUP"


    fun showTextNotification(context: Context, title: String, message: String) {
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()
        val channel = createNotificationChannel(context, channelId, channelName)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = getBaseNotificationBuilder(context, title, message, channelId)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        notificationManager.notify(uniId, notificationBuilder.build())
    }
    /**
     * 액티비티용  코루틴
     */
    fun showImgNotification(
        context: Context,
        title: String,
        message: String,
        pushImageUrl: String
    ) {
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        val img = ImgUtil.urlToBitmap(context, pushImageUrl) // 이미지 변환 로직 필요

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = createNotificationChannel(context, channelId, channelName)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = getBaseNotificationBuilder(context, title, message, channelId)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(img).setBigContentTitle(title).setSummaryText(message))
                .setLargeIcon(img)

        notificationManager.notify(uniId, notificationBuilder.build())
    }

    /**
     * 서비스용 코루틴
     */
    fun showImgNotification(
        context: Context,
        serviceScope: CoroutineScope,
        title: String,
        message: String,
        pushImageUrl: String
    ) {
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()
        serviceScope.launch(Dispatchers.IO) {
            val img = ImgUtil.urlToBitmap(context, pushImageUrl) // 이미지 변환 로직 필요

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = createNotificationChannel(context, channelId, channelName)
            notificationManager.createNotificationChannel(channel)

            val notificationBuilder = getBaseNotificationBuilder(context, title, message, channelId)
                .setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(img).setBigContentTitle(title)
                        .setSummaryText(message)
                )
                .setLargeIcon(img)

            notificationManager.notify(uniId, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String, channelName: String): NotificationChannel {
        return NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
            description = channelName
            setBypassDnd(true) // 방해 금지 모드 우회 설정 (필요에 따라)
        }
    }

    private fun getBaseNotificationBuilder(context: Context, title: String, message: String, channelId: String): NotificationCompat.Builder {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 설정
            .setAutoCancel(true)
            .setSound(soundUri) // 알림 소리
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 알림 중요도 설정
            .setColor(ContextCompat.getColor(context, R.color.color_main)) // 알림 색상 설정
            .setLights(Color.BLUE, 3000, 3000) // LED 색상 및 깜빡임 설정
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // 진동 패턴 설정
            .setGroup(groupKey)
//            .setGroupSummary(true)
    }
}
