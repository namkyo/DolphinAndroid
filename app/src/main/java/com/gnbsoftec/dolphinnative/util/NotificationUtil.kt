package com.gnbsoftec.dolphinnative.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.R
import com.gnbsoftec.dolphinnative.extension.decodeUrl
import com.gnbsoftec.dolphinnative.service.FcmModel
import com.gnbsoftec.dolphinnative.view.WebViewActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object NotificationUtil {
    private const val channelId = "channel_gnb"
    private const val channelName = "gnb"
    private const val groupKey  = BuildConfig.APPLICATION_ID+".NOTIFICATION_GROUP"

    fun showNomalNoti(context: Context, title: String , message: String) {
        GLog.d("이미지 알림")
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        /** [3]  알림 채널 생성  */
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(createNotificationChannel())

        /** [4]  다운로드받은 이미지 등록  */
        val notificationBuilder = getBaseNotificationBuilder(context, title, message)
            .setStyle(
                NotificationCompat.BigPictureStyle().setBigContentTitle(title)
                    .setSummaryText(message)
            )

        /** [5] 알림 실행  */
        notificationManager.notify(uniId, notificationBuilder.build())
    }
    /**
     * 액티비티용  코루틴
     */
    fun showNotiActivity(context: Context, title: String , message: String , imageYn : Boolean ,file: File) {
        GLog.d("이미지 알림")
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()
        //코루틴 백그라운드
        /** [1]  URL => File 다운로드  */
        val fileUri = FileUtil.fileToUri(context,file)
        val img = ImgUtil.uriToBitmap(context,fileUri)

        /** [3]  알림 채널 생성  */
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(createNotificationChannel())

        /** [4]  다운로드받은 이미지 등록  */
        val notificationBuilder = getBaseNotificationBuilder(context, title, message)


        if(imageYn){
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(img).setBigContentTitle(title)
                    .setSummaryText(message)
            )
        }else{
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle().setBigContentTitle(title)
                    .setSummaryText(message)
            )
        }
        notificationBuilder.setContentIntent(makeAlbumIntent(context, FileUtil.fileToUri(context,file))) // PendingIntent 설정

        /** [5] 알림 실행  */
        notificationManager.notify(uniId, notificationBuilder.build())
    }

    /**
     * 서비스애서 실행하는 알림 코루틴 적용
     */
    fun showNotiService(context: Context,serviceScope: CoroutineScope,fcmModel: FcmModel.Message) {
        GLog.d("이미지 알림")
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()
        //코루틴 백그라운드
        /** [1]  코루틴 백그라운드 실행 */
        serviceScope.launch(Dispatchers.IO) {

            /** [3]  알림 채널 생성  */
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(createNotificationChannel())

            /** [4]  다운로드받은 이미지 등록  */
            val notificationBuilder = getBaseNotificationBuilder(context, fcmModel.pushTitle, fcmModel.pushMessage).apply {

                if(TextUtils.isEmpty(fcmModel.pushImageUrl)){
                    setStyle(
                        NotificationCompat.BigPictureStyle().setBigContentTitle(fcmModel.pushTitle)
                            .setSummaryText(fcmModel.pushMessage)
                    )
                    setContentIntent(makePushIntent(context,fcmModel.pushClickLink)) // PendingIntent 설정
                }else{
                    /** [2]  URL => File 다운로드  */
                    val img = ImgUtil.urlToBitmap(context, fcmModel.pushImageUrl)
                    setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(img).setBigContentTitle(fcmModel.pushTitle)
                            .setSummaryText(fcmModel.pushMessage)
                    )
                    setLargeIcon(img)
                    setContentIntent(makePushIntent(context,fcmModel.pushClickLink)) // PendingIntent 설정
                }
            }


            /** [5] 알림 실행  */
            notificationManager.notify(uniId, notificationBuilder.build())
        }
    }

    /**
     * 알림 채널 생성
     */
    private fun createNotificationChannel(): NotificationChannel {
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

    private fun getBaseNotificationBuilder(context: Context, title: String, message: String): NotificationCompat.Builder {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_dolphin) // 아이콘 설정
            .setAutoCancel(true)
            .setSound(soundUri) // 알림 소리
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 알림 중요도 설정
            .setColor(ContextCompat.getColor(context, R.color.color_main)) // 알림 색상 설정
            .setLights(Color.BLUE, 3000, 3000) // LED 색상 및 깜빡임 설정
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) // 진동 패턴 설정
            .setGroup(groupKey)
            .setGroupSummary(true)
    }

    /**
     * 알림선택 푸쉬 링크 주입 인텐트
     */
    private fun makePushIntent(context:Context,url:String):PendingIntent{
        val gson = Gson()
        val intent = Intent(context, WebViewActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            if(TextUtils.isEmpty(url)){
                putExtra(PreferenceUtil.keys.PUSH_URL,gson.toJson(HashMap<String,String>().apply {
                    put("link","")
                }))
            }else{
                putExtra(PreferenceUtil.keys.PUSH_URL,gson.toJson(HashMap<String,String>().apply {
                    put("link",url.decodeUrl())
                }))
            }
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }else{
            PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent,PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return pendingIntent
    }

    /**
     * 알림 선택 앨범이동
     */
    private fun makeAlbumIntent(context: Context, uri: Uri): PendingIntent {
        // 인텐트를 명시적으로 만듭니다.
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Android 12 (API 레벨 31) 이상에서는 FLAG_IMMUTABLE을 사용해야 합니다.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, flags)
    }

}
