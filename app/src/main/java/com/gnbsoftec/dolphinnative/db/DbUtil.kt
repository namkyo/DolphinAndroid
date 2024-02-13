package com.gnbsoftec.dolphinnative.db

import android.content.ContentValues
import android.content.Context
import com.gallery.orix.db.PushMessageDbHelper
import com.gallery.orix.db.PushMessageEntry
import com.gnbsoftec.dolphinnative.service.FcmModel
import com.gnbsoftec.dolphinnative.util.DateUtil
import com.gnbsoftec.dolphinnative.util.GLog

object DbUtil {

    // 푸시 메시지를 데이터베이스에 삽입하는 내부 함수입니다.
    fun insertPushMessage(context: Context, pushMsg: FcmModel.Message) {
        PushMessageDbHelper.getInstance(context).writableDatabase.use { db ->
            ContentValues().apply {
                // 각 컬럼에 데이터를 매핑합니다.
                put(PushMessageEntry.COLUMN_NAME_TITLE, pushMsg.pushTitle)
                put(PushMessageEntry.COLUMN_NAME_MESSAGE, pushMsg.pushMessage)
                put(PushMessageEntry.COLUMN_NAME_IMAGE_URL, pushMsg.pushImageUrl)
                put(PushMessageEntry.COLUMN_NAME_CLICK_LINK, pushMsg.pushClickLink)
                put(PushMessageEntry.COLUMN_NAME_PUSH_DATE, DateUtil.getTimestamp("yyyyMMddHHmmss"))
            }.also { values ->
                // ContentValues를 사용하여 데이터베이스에 새 행을 삽입합니다.
                db.insert(PushMessageEntry.TABLE_NAME, null, values).also { newRowId ->
                    GLog.d("insertPushMessage.newRowId : $newRowId")
                }
            }
        }
    }


    // 데이터베이스에 저장된 모든 푸시 메시지를 JSONArray 형태로 반환하는 함수입니다.
    fun getAllPushMessages(context: Context): List<Map<String, String>> {
        return ArrayList<Map<String, String>>().apply {
            PushMessageDbHelper.getInstance(context).readableDatabase.use { db ->
                val orderBy = "${PushMessageEntry.COLUMN_NAME_PUSH_DATE} DESC"
                db.query(PushMessageEntry.TABLE_NAME, null, null, null, null, null, orderBy)
                    .use { cursor ->
                        val columnNames = cursor.columnNames
                        // 커서를 이동시키면서 각 메시지를 JSONObject로 변환하여 JSONArray에 추가합니다.
                        while (cursor.moveToNext()) {
                            val messageObject = HashMap<String, String>().apply {
                                columnNames.forEach { columnName ->
                                    put(
                                        columnName,
                                        cursor.getString(cursor.getColumnIndexOrThrow(columnName))
                                    )
                                }
                            }
                            add(messageObject) // 올바른 메소드 호출로 변경됨
                        }
                    }
            }
        }
    }

    // 데이터베이스의 모든 푸시 메시지를 삭제하는 함수입니다.
    fun deleteAllPushMessages(context: Context): Int = PushMessageDbHelper.getInstance(context).writableDatabase.use { db ->
        val result = db.delete(PushMessageEntry.TABLE_NAME, null, null)
        GLog.d("All rows in ${PushMessageEntry.TABLE_NAME} deleted") // 삭제 로그를 남깁니다.
        result
    }
}
