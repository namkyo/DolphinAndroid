package com.gnbsoftec.dolphinnative.util

import android.app.Activity
import android.content.Context
import java.util.HashSet

object PreferenceUtil {
    private const val PREF_KEY = "HSBPrefs"

    object keys{
        const val PUSH_KEY = "PUSH_KEY" //푸쉬 UUID
        const val PUSH_YN = "PUSH_YN" //푸쉬 여용 여부 Y/N
    }
    fun put(context: Context,key: String, value: String) {
        val pref = context.getSharedPreferences(
            PREF_KEY,
            Activity.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun put(context: Context,key: String, value: Boolean) {
        val pref = context.getSharedPreferences(
            PREF_KEY,
            Activity.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun put(context: Context,key: String, value: Int) {
        val pref = context.getSharedPreferences(
            PREF_KEY,
            Activity.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun put(context: Context,key: String, value: HashSet<String>) {
        val pref = context.getSharedPreferences(
            PREF_KEY,
            Activity.MODE_PRIVATE
        )
        val editor = pref.edit()

        editor.putStringSet(key, value)
        editor.apply()
    }

    fun getValue(context: Context,key: String, dftValue: String): String {
        return try {
            val pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE)
            "${pref.getString(key, dftValue)}"
        } catch (e: NullPointerException) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        } catch (e: Exception) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        }
    }

    fun getValue(context: Context,key: String, dftValue: Int): Int {
        return try {
            val pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE)
            pref.getInt(key, dftValue)
        } catch (e: NullPointerException) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        } catch (e: Exception) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        }
    }

    fun getValue(context: Context,key: String, dftValue: Boolean): Boolean {
        return try {
            val pref = context.getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE)
            pref.getBoolean(key, dftValue)
        } catch (e: NullPointerException) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        } catch (e: Exception) {
            GLog.e("에러입니다 [${e.message}]")
            dftValue
        }
    }


    // 값(ALL Data) 삭제하기
    fun removeAllPreferences(context: Context) {
        val pref = context.getSharedPreferences(
            PREF_KEY,
            Activity.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }
}