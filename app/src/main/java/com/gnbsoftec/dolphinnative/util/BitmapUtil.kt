package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
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

    fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
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

    fun fileToBitmap(file: File):Bitmap{
        val filePath = file.getPath()
        return BitmapFactory.decodeFile(filePath)
    }

    fun imgResize(activity: Activity,file: File): Uri? {
        return bitmapToUri(activity,resizeBitmap(fileToBitmap(file),1024))
    }
}