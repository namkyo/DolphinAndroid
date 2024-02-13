package com.gallery.orix.db

object PushMessageEntry {
    const val TABLE_NAME = "push_messages"
    //primary key
    const val COLUMN_NAME_ID = "id"
    //mMiAps fcm
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_MESSAGE = "message"
    const val COLUMN_NAME_IMAGE_URL = "image_url"
    const val COLUMN_NAME_CLICK_LINK = "click_link"
    const val COLUMN_NAME_PUSH_DATE = "push_date"

    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_NAME_ID INTEGER PRIMARY KEY," +
                "$COLUMN_NAME_TITLE TEXT," +
                "$COLUMN_NAME_MESSAGE TEXT," +
                "$COLUMN_NAME_IMAGE_URL TEXT," +
                "$COLUMN_NAME_CLICK_LINK TEXT," +
                "$COLUMN_NAME_PUSH_DATE TEXT"

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
}