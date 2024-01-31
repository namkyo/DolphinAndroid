package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.R
import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


object BitmapUtil {
    fun urlToBitmap(urlStr: String): Bitmap {
        val url = URL(urlStr)
        return BitmapFactory.decodeStream(url.openStream())
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "gnb_img_" + timeStamp + "_"
        val storageDir = File(Constants.FILE_SAVE_PATH+ Constants.FILE_SAVE_SUB_PATH)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    fun uriToBitmap(activity: Activity,imageuri: Uri): Bitmap? {
        var bm: Bitmap? = null
        try {
            bm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.contentResolver, imageuri))
            } else {
                MediaStore.Images.Media.getBitmap(activity.contentResolver, imageuri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bm
    }

    fun resizeBitmap(bitmap: Bitmap,newWidth:Int):Bitmap {
        Logcat.d("원본 width ${bitmap.width}")
        Logcat.d("원본 height ${bitmap.height}")

        val scale = BigDecimal(newWidth).divide(BigDecimal(bitmap.width),5,RoundingMode.CEILING)
        Logcat.d("scale $scale")
        val newHeight = BigDecimal(bitmap.height).multiply(scale)
        Logcat.d("newHeight $newHeight")

        val resizedBmp = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight.toInt(), true)
        Logcat.d("압축 width ${resizedBmp.width}")
        Logcat.d("압축 height ${resizedBmp.height}")
        return resizedBmp
    }

    fun bitmapToUri(activity: Activity, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        }
        val path = MediaStore.Images.Media.insertImage(activity.contentResolver, bitmap, "Title" + " - " + Calendar.getInstance().getTime(), null)
        return Uri.parse(path)
    }
    /**
     * file -> bitmap
     */
    fun fileToBitmap(file: File):Bitmap{
        val filePath = file.getPath()
        return BitmapFactory.decodeFile(filePath)
    }

    /**
     * bitmap -> file
     */
    fun bitmapToFile(bitmap: Bitmap): File? {
        val strFilePath = Constants.FILE_SAVE_PATH+ Constants.FILE_SAVE_SUB_PATH
        val file = File(strFilePath)
        if (!file.exists()) file.mkdirs()
        val fileCacheItem = createImageFile()!!
        var out: OutputStream? = null
        try {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return fileCacheItem
    }


    fun imgResize(activity: Activity,uri: Uri): File? {
        val resizeBitmap = resizeBitmap(uriToBitmap(activity,uri)!!,2048)
        val resizeFile = bitmapToFile(resizeBitmap)
        return resizeFile
    }


    fun getOutputDirectory(activity: Activity): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else activity.filesDir
    }
}