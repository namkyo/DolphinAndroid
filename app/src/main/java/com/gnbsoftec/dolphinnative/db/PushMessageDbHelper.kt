package com.gallery.orix.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PushMessageDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PushMessageEntry.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(PushMessageEntry.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "PushMessages.db"

        @Volatile
        private var instance: PushMessageDbHelper? = null

        fun getInstance(context: Context): PushMessageDbHelper =
            instance ?: synchronized(this) {
                instance ?: PushMessageDbHelper(context.applicationContext).also { instance = it }
            }
    }
}

