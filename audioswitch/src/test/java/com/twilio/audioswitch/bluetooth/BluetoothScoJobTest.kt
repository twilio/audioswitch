package com.twilio.audioswitch.bluetooth

import android.media.AudioManager
import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.assertScoJobIsCanceled
import com.twilio.audioswitch.bluetooth.BluetoothScoJob.BluetoothScoRunnable
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.setupScoHandlerMock
import com.twilio.audioswitch.setupSystemClockMock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BluetoothScoJobTest {

    private val logger = mock<LogWrapper>()
    private var handler = setupScoHandlerMock()
    val audioManager = mock<AudioManager>()
    private val audioDeviceManager = AudioDeviceManager(mock(),
            logger,
            audioManager,
            mock(),
            mock())
    private var systemClockWrapper = setupSystemClockMock()
    private val deviceCache = BluetoothHeadsetCacheManager(logger)
    private val headsetState = HeadsetState(logger)
    private var scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, headsetState, handler, systemClockWrapper)

    @Test
    fun `EnableBluetoothScoJob scoAction should execute enableBluetoothSco with true`() {
        scoJob.executeBluetoothScoJob()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `EnableBluetoothScoJob scoTimeOutAction should not remove the active bluetooth headset if it doesn't exist`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, headsetState, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        assertThat(deviceCache.cachedHeadsets.isEmpty(), equalTo(true))
    }

    @Test
    fun `DisableBluetoothScoJob should execute enableBluetoothSco with false`() {
        val scoJob = DisableBluetoothScoJob(logger, audioDeviceManager, headsetState, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager).stopBluetoothSco()
    }

    @Test
    fun `BluetoothScoRunnable should execute enableBluetoothSco multiple times if not canceled`() {
        handler = mock {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }

            var firstInvocation = true
            whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                if (firstInvocation) {
                    firstInvocation = false
                    (it.arguments[0] as BluetoothScoRunnable).run()
                }
                true
            }
        }
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, headsetState, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager, times(2)).startBluetoothSco()
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime equals the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, headsetState, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        assertScoJobIsCanceled(handler, scoJob)
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime is greater than the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT + 1000)
        }
        handler = setupHandlerMock()
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, headsetState, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager).startBluetoothSco()
        assertScoJobIsCanceled(handler, scoJob)
    }

    @Test
    fun `cancelBluetoothScoJob should cancel sco runnable`() {
        scoJob.executeBluetoothScoJob()
        scoJob.cancelBluetoothScoJob()

        assertScoJobIsCanceled(handler, scoJob)
    }

    @Test
    fun `cancelBluetoothScoJob should not cancel sco runnable if it has not been initialized`() {
        scoJob.cancelBluetoothScoJob()

        verifyZeroInteractions(handler)
    }

    private fun setupHandlerMock() =
        mock<Handler> {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }

            whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }
        }
}
