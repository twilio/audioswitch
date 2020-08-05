package com.twilio.audioswitch

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.Logger.Level.ERROR
import com.twilio.audioswitch.Logger.Level.WARN
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper
import org.junit.Test

private const val TAG = "TAG"
private const val MESSAGE = "message"

class LoggerTest {

    private var buildWrapper = mock<BuildWrapper> {
        whenever(mock.buildType).thenReturn(DEBUG)
    }
    private val logWrapper = mock<LogWrapper>()
    private var logger = Logger(true, logWrapper, buildWrapper)

    @Test
    fun `log should not log if logging is disabled and build type is release`() {
        whenever(buildWrapper.buildType).thenReturn("release")
        logger = Logger(false, logWrapper, buildWrapper)
        logger.log(TAG, MESSAGE)

        verifyZeroInteractions(logWrapper)
    }

    @Test
    fun `log should log if logging is enabled and build type is release`() {
        whenever(buildWrapper.buildType).thenReturn("release")
        logger.log(TAG, MESSAGE, WARN)

        verify(logWrapper).w(TAG, MESSAGE)
    }

    @Test
    fun `log should log if logging is disabled and build type is debug`() {
        logger = Logger(false, logWrapper, buildWrapper)
        logger.log(TAG, MESSAGE, ERROR())

        verify(logWrapper).e(TAG, MESSAGE)
    }

    @Test
    fun `log should log if logging is enabled and build type is debug`() {
        val throwable = Throwable()
        logger.log(TAG, MESSAGE, ERROR(throwable = throwable))

        verify(logWrapper).e(TAG, MESSAGE, throwable)
    }

    @Test
    fun `warning log should log if logging is enabled`() {
        logger.log(TAG, MESSAGE, WARN)

        verify(logWrapper).w(TAG, MESSAGE)
    }

    @Test
    fun `error log should log if logging is enabled`() {
        logger.log(TAG, MESSAGE, ERROR())

        verify(logWrapper).e(TAG, MESSAGE)
    }

    @Test
    fun `error log with throwable should log if logging is enabled`() {
        val throwable = Throwable()
        logger.log(TAG, MESSAGE, ERROR(throwable))

        verify(logWrapper).e(TAG, MESSAGE, throwable)
    }
}
