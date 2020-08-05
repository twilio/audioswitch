package com.twilio.audioswitch

import com.twilio.audioswitch.Logger.Level.ERROR
import com.twilio.audioswitch.Logger.Level.WARN
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper

internal const val DEBUG = "debug"

internal class Logger(
    private val isEnabled: Boolean,
    private val logWrapper: LogWrapper = LogWrapper(),
    private val buildWrapper: BuildWrapper = BuildWrapper()
) {

    fun log(tag: String, message: String, level: Level = Level.DEBUG) {
        if (buildWrapper.buildType == DEBUG || isEnabled) {
            when (level) {
                Level.DEBUG -> logWrapper.d(tag, message)
                WARN -> logWrapper.w(tag, message)
                is ERROR -> {
                    level.throwable?.let {
                        logWrapper.e(tag, message, it)
                    } ?: logWrapper.e(tag, message)
                }
            }
        }
    }

    sealed class Level {
        object DEBUG : Level()
        object WARN : Level()
        data class ERROR(val throwable: Throwable? = null) : Level()
    }
}
