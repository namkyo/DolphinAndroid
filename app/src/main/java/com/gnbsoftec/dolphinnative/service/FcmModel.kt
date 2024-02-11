package com.gnbsoftec.dolphinnative.service

class FcmModel {
    data class Message(
        val pushTitle: String,
        val pushMessage: String,
        val pushImageUrl: String,
        val pushClickLink: String
    )
}