package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.gnbsoftec.dolphinnative.service.FcmModel
import java.io.File
import java.net.URLDecoder

object FileUtil {
    private var downReceiver: BroadcastReceiver? = null

    private var mDownloadQueueId : Long? = null
    private var mDownloadManager : DownloadManager? = null

    fun urlFileDownload(activity: Activity, fileUrl:String, callback:((Boolean, String) -> Unit)){
        val decodedString = URLDecoder.decode(fileUrl, "UTF-8").split("/")
        val fileName = decodedString[decodedString.size-1]

        // 다운로드 리스너 등록
        val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        // 이전 리시버 해제
        downReceiver?.let {
            activity.applicationContext.unregisterReceiver(it)
            downReceiver = null
        }

        downReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                if(mDownloadQueueId != null && mDownloadQueueId == reference) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(reference)
                    val cursor: Cursor? = mDownloadManager?.query(query)

                    cursor?.moveToFirst()

                    val columnIndex = cursor?.getColumnIndex(DownloadManager.COLUMN_STATUS)

                    val status = columnIndex?.let { cursor.getInt(it) }

                    cursor?.close()
                    VibratorUtil.vibrator(activity.applicationContext)
                    when(status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            GLog.d("DownloadManager.STATUS_SUCCESSFUL")
                            callback(true, "정상 다운로드")
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            GLog.d("DownloadManager.STATUS_PAUSED")
                            callback(false, "취소 되었습니다.")
                        }
                        else -> {
                            GLog.d("DownloadManager.STATUS_FAILED")
                            callback(false,"저장 중 오류가 발생하였습니다. [$status]")
                        }
                    }
                }
            }
        }
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                activity.applicationContext.registerReceiver(downReceiver, completeFilter, Context.RECEIVER_EXPORTED)
            }else{
                activity.applicationContext.registerReceiver(downReceiver, completeFilter, Context.RECEIVER_EXPORTED)
            }

            mDownloadManager = activity.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request = DownloadManager.Request(Uri.parse(fileUrl)).apply {
                setDescription("$fileName 다운로드")
//                setMimeType(fileName.split(".")[1])
//                                addRequestHeader("User-Agent", userAgent)
                setAllowedOverMetered(true)//모바일네트워크가 연결되었을 때도 다운로드
                setAllowedOverRoaming(true)
                setTitle(fileName)
                setRequiresCharging(false)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                allowScanningByMediaScanner()
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }

            mDownloadQueueId = mDownloadManager?.enqueue(request)

        }catch (e:Exception){
            GLog.e("파일다운로드 에러",e)
        }
    }
    fun base64FileDownload(activity:Activity,fileName:String,base64Str:String,callback:((Boolean,String) -> Unit)){
        val mimeType = fileName.split(".")[1]

        try{
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

            GLog.d("fileName : $fileName")
            GLog.d("mimeType : $mimeType")
            GLog.d("path : $path")
            val file = makeForFile(path, fileName, fileName, mimeType, 1)
            file.writeBytes(base64Str.toByteArray(Charsets.UTF_8))
            GLog.d("path : $path")
            GLog.d("decodeStr : $base64Str")
            GLog.d("file.absolutePath : ${file.absolutePath}")
            GLog.d("file.name : ${file.name}")
            GLog.d("file.isFile : ${file.isFile}")
            GLog.d("file.length : ${file.length()}")
            ToastUtil.show(activity, "${file.name} 다운로드 시작..", 1000)

            Handler(Looper.getMainLooper()).postDelayed({
                //갤러리 새로고침
                scanFile(activity, file, mimeType)

                callback(true,"정상 다운로드")

                //알림띄우기
                val imageYn = mimeType != "pdf"
                NotificationUtil.showNotiActivity(activity.applicationContext,"오릭스캐피탈","${file.name} 다운로드완료",imageYn,file)

                //실행할 코드
            }, 1000)
        }catch (e:Exception){
            GLog.e("에러",e)
            callback(false,e.message.toString())
        }
    }

    /**
     * 파일 쓰기시 중복일때 카운트 하나 늘려서 파일 생성
     */
    private fun makeForFile(path: String, name: String, next: String, mime: String, count: Int) : File {
        val file = File(path, "$next.$mime")
        return if(file.isFile){
            val cnt=count+1
            val nextName = name+"_"+cnt
            makeForFile(path, name, nextName, mime, cnt)
        }else{
            file
        }
    }
    /**
     * 갤러리 새로고침
     */
    private fun scanFile(ctxt: Context, f: File, mimeType: String) {
        MediaScannerConnection.scanFile(ctxt, arrayOf(f.absolutePath), arrayOf(mimeType), null)
    }


    fun fileToUri(context: Context,file: File):Uri{
        return FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.provider",
            file
        )
    }

    /**
     * 웹뷰에서 파일 다운로드 리스너
     */
    private const val myPath = "/Dolphin/"
    fun fileDownload(activity: Activity,url:String, userAgent:String, contentDisposition:String, mimetype:String, contentLength:Long){
        GLog.d("================setDownloadListener=================")
        GLog.d("url = $url")
        GLog.d("userAgent = $userAgent")
        GLog.d("contentDisposition = $contentDisposition")
        GLog.d("mimetype = $mimetype")
        GLog.d("contentLength = $contentLength")

        val downloadManager = activity.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager

        // 파일명 정제 시작
        val urlSplit = url.split("/")
        val fileName = urlSplit[(urlSplit.size-1)]
        // 파일명 정제 끝

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimetype)
            addRequestHeader("User-Agent", userAgent)
            setDescription("파일 다운로드")
            setAllowedOverMetered(true)//모바일네트워크가 연결되었을 때도 다운로드
            setAllowedOverRoaming(true)
            setTitle(fileName)
            setRequiresCharging(false)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            allowScanningByMediaScanner()
            getPath()
            val fileSetName = myPath+fileName
            GLog.d("fileName : $fileSetName")
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileSetName)
        }
        registerDownloadReceiver(downloadManager, activity)

        try {
            downloadId = downloadManager.enqueue(request)
//            mainActivity.showToast("다운로드 시작중..")
        }catch (e:Exception){
//            mainActivity.showToast("다운로드 에러")
            ErrorUtil.errorPress(e)
        }
    }

    /**
     * 다운로드 상태 리시버
     */
    private var downloadId : Long = 0

    private fun registerDownloadReceiver(downloadManager: DownloadManager, activity: Activity) {
        val downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                when (intent?.action) {
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                        if(downloadId == id){
                            val query: DownloadManager.Query = DownloadManager.Query()
                            query.setFilterById(id)
                            val cursor = downloadManager.query(query)
                            if (!cursor.moveToFirst()) {
                                return
                            }
                            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloadId = 0
//                                BitmapUtil.gallReload(mainActivity.applicationContext)
//                                mainActivity.showToast("다운로드가 완료됐습니다.")
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                downloadId = 0
//                                mainActivity.showToast("다운로드가 실패했습니다.")
                            }
                        }
                    }
                }
            }
        }
        IntentFilter().run {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                activity.applicationContext.registerReceiver(downloadReceiver, this, Context.RECEIVER_EXPORTED)
            }else{
                activity.applicationContext.registerReceiver(downloadReceiver, this, Context.RECEIVER_EXPORTED)
            }
        }
    }

    private fun getPath(): String {
        val dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + myPath
        val file = File(dirPath)
        if (!file.exists())
            file.mkdirs()
        return dirPath
    }
}