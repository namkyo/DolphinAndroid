package com.gnbsoftec.dolphinnative.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

object DateUtil {
    @SuppressLint("SimpleDateFormat")
    fun getTimestamp(format: String): String {
        val dateFormatter = SimpleDateFormat(format)
        val currentDateTime = Date()
        return dateFormatter.format(currentDateTime)
    }
}