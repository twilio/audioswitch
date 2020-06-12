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
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BluetoothScoJob.BluetoothScoRunnable
import com.twilio.audioswitch.selection.AudioDeviceManager
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BluetoothScoJobTest {

    private val logger = mock<LogWrapper>()
    private var handler = mock<Handler> {
        whenever(mock.post(any())).thenAnswer {
            (it.arguments[0] as BluetoothScoRunnable).run()
            true
        }
    }
    val audioManager = mock<AudioManager>()
    private val audioDeviceManager = AudioDeviceManager(mock(),
            logger,
            audioManager,
            mock(),
            mock())
    private var systemClockWrapper = mock<SystemClockWrapper> {
        whenever(mock.elapsedRealtime()).thenReturn(0)
    }
    private var scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper)

    @Test
    fun `EnableBluetoothScoJob should execute enableBluetoothSco with true`() {
        scoJob.executeBluetoothScoJob()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `DisableBluetoothScoJob should execute enableBluetoothSco with false`() {
        val scoJob = DisableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper)

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
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager, times(2)).startBluetoothSco()
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime equals the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = mock {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }

            whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }
        }
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager).startBluetoothSco()
        assertCanceled()
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime is greater than the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT + 1000)
        }
        handler = mock {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }

            whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                (it.arguments[0] as BluetoothScoRunnable).run()
                true
            }
        }
        scoJob = EnableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper)

        scoJob.executeBluetoothScoJob()

        verify(audioManager).startBluetoothSco()
        assertCanceled()
    }

    @Test
    fun `cancelBluetoothScoJob should cancel sco runnable if it is running`() {
        scoJob.executeBluetoothScoJob()
        scoJob.cancelBluetoothScoJob()

        assertCanceled()
    }

    @Test
    fun `executeBluetoothScoJob should not cancel sco runnable if it is not running`() {
        scoJob.cancelBluetoothScoJob()

        verifyZeroInteractions(handler)
    }

    @Test
    fun `executeBluetoothScoJob should only allow a single running job`() {
        scoJob.executeBluetoothScoJob()
        scoJob.executeBluetoothScoJob()

        verify(handler).post(isA())
        verify(audioManager).startBluetoothSco()
    }

    private fun assertCanceled() {
        verify(handler).removeCallbacks(isA())
        assertThat(scoJob.bluetoothScoRunnable, `is`(nullValue()))
    }
}
