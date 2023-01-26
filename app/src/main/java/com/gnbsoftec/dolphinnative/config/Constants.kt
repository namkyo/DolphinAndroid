package com.gnbsoftec.dolphinnative.config

import android.os.Environment

/*
 * [Checklist]
 */
object Constants {
    const val SERVER_URL: String = "http://175.209.155.74:8180/login.frm"

    val FILE_SAVE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    const val FILE_SAVE_SUB_PATH = "/GNB/"

    object pushIntentData {
        const val pushUrl: String = "pushUrl"
        const val title: String = "title"
        const val body: String = "body"
        const val image: String = "image"
    }
}