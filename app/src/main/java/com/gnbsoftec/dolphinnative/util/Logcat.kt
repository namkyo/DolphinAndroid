package com.gnbsoftec.dolphinnative.util

import android.os.Environment
import android.util.Log
import com.gnbsoftec.dolphinnative.BuildConfig
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class Logcat {
    companion object {
         const val TAG: String = "DolphinLog"

        fun d(message: String) {
            if (BuildConfig.DEBUG) Log.d(TAG, buildLogMessage(message))
        }

        fun d(tag : String,message: String) {
            if (BuildConfig.DEBUG) Log.d(tag, buildLogMessage(message))
        }


        fun e(message: String) {
            if (BuildConfig.DEBUG) Log.e(TAG, buildLogMessage(message))
        }


        fun i(message: String) {
            if (BuildConfig.DEBUG) Log.i(TAG, buildLogMessage(message))
        }


        fun w(message: String) {
            if (BuildConfig.DEBUG) Log.w(TAG, buildLogMessage(message))
        }


        fun v(message: String) {
            if (BuildConfig.DEBUG) Log.v(TAG, buildLogMessage(message))
        }


        private fun buildLogMessage(message: String): String {
            val ste = Thread.currentThread().stackTrace[4]
            val sb = StringBuilder()
            sb.append("[")
            sb.append(ste.fileName)
            sb.append("] ")
            sb.append(ste.methodName)
            sb.append(" #")
            sb.append(ste.lineNumber)
            sb.append(": ")
            sb.append(message)
            // writeLog(sb.toString())
            return sb.toString()
        }
    }
}