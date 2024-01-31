package com.gnbsoftec.dolphinnative.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object CoroutineUtil {
    suspend fun setInterval(period: Long, action: suspend () -> Unit) {
        coroutineScope {
            while (isActive) {  // isActive는 CoroutineScope의 프로퍼티입니다.
                action()
                delay(period)
            }
        }
    }
}