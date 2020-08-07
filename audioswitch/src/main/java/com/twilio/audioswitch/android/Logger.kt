package com.twilio.audioswitch.android

import android.util.Log
import com.twilio.audioswitch.BuildConfig

internal class Logger(var loggingEnabled: Boolean = BuildConfig.DEBUG) {

    fun d(tag: String, message: String) {
        if (loggingEnabled) {
            Log.d(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (loggingEnabled) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (loggingEnabled) {
            Log.e(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        if (loggingEnabled) {
            Log.e(tag, message, throwable)
        }
    }
}
