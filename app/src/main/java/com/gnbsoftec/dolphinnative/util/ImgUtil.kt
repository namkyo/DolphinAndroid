package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object ImgUtil {
    /**
     * uri => Bitmap
     */
    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver,uri))
        }else{
            @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver,uri)
        }
        return bitmap
    }

    fun urlToBitmap(context: Context, pushImageUrl: String): Bitmap? {
        return try {
            val url = URL(pushImageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            return BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: Exception) {
            GLog.e("이미지 다운로드 에러",e)
            null
        }
    }

    fun resizeImageFile(photoFile: File, width: Int): File? {
        GLog.d("원본 이미지 크기 : ${photoFile.length()}")
        return try {
            // 비트맵으로 이미지 로드
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

            // 새로운 높이 계산 (가로 세로 비율 유지)
            val height = bitmap.height * width / bitmap.width

            // 리사이즈된 비트맵 생성
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            // 리사이즈된 이미지를 새 파일에 저장
            val resizedFile = File.createTempFile("resized_", ".png", photoFile.parentFile)
            resizedFile.outputStream().use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            GLog.d("압축 이미지 크기 : ${resizedFile.length()}")
            // 리사이즈된 파일 반환
            resizedFile
        } catch (e: Exception) {
            ErrorUtil.errorPress(e)
            null
        }
    }


    fun convertImageToBase64(activity: Activity, uri: Uri): String {
        val inputStream = activity.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun resizeImage(context: Context, uri: Uri, maxSize: Int): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        var width = options.outWidth
        var height = options.outHeight
        var inSampleSize = 1

        while (width / 2 > maxSize || height / 2 > maxSize) {
            width /= 2
            height /= 2
            inSampleSize *= 2
        }

        val resizedInputStream = context.contentResolver.openInputStream(uri) ?: return null
        options.apply {
            inJustDecodeBounds = false
        }
        val resizedBitmap = BitmapFactory.decodeStream(resizedInputStream, null, options)
        resizedInputStream.close()

        return resizedBitmap
    }

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun resizeImageFromUri(context: Context, imageUri: Uri, width: Int): Bitmap? {
        try {
            // URI로부터 Bitmap 읽어오기
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                // 이미지가 요청한 너비보다 작거나 같은 경우, 원본 이미지 반환
                if (originalBitmap.width <= width) {
                    return originalBitmap
                }

                // 새로운 높이 계산 (가로 세로 비율 유지)
                val height = originalBitmap.height * width / originalBitmap.width

                // 리사이즈된 비트맵 생성
                return Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            }
        } catch (e: Exception) {
            e.printStackTrace() // 로그에 오류 출력
            return null
        }
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun resizeDrawable(drawable: Drawable, width: Int): Drawable? {
        try {
            // Drawable을 Bitmap으로 변환
            val bitmap = drawableToBitmap2(drawable)

            // 새로운 높이 계산 (가로 세로 비율 유지)
            val height = bitmap.height * width / bitmap.width

            // 리사이즈된 비트맵 생성
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            // Bitmap을 Drawable로 변환하여 반환
            return BitmapDrawable(resizedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Drawable을 비트맵으로 변환
    fun drawableToBitmap2(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        // Drawable을 Bitmap으로 변환
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun resizeBitmap(bitmap: Bitmap, width: Int): Bitmap {
        val height = bitmap.height * width / bitmap.width
        // 리사이즈된 비트맵 생성
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}