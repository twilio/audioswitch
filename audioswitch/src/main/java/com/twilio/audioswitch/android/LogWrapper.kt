package com.twilio.audioswitch.android

import android.util.Log

internal class LogWrapper {

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }
}
