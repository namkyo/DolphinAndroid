package com.gnbsoftec.dolphinnative.util

import com.gnbsoftec.dolphinnative.common.Constants
import com.gnbsoftec.dolphinnative.BuildConfig
import timber.log.Timber

object Glog {
    private const val TAG = BuildConfig.APPLICATION_ID
    fun d(message: String) {
        if (Constants.IS_DEBUG) Timber.tag(TAG).d(buildLogMessage(message))
    }
    fun e(message: String) {
        if (Constants.IS_DEBUG) Timber.tag(TAG).e(buildLogMessage(message))
    }
    fun e(message: String,e:Throwable) {
        if (Constants.IS_DEBUG) Timber.tag(TAG).e(e, buildLogMessage(message))
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
        return sb.toString()
    }
}