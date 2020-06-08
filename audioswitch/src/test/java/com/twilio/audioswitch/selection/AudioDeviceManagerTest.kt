package com.twilio.audioswitch.selection

import android.media.AudioManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.AsyncTaskWrapper
import com.twilio.audioswitch.android.ThreadWrapper
import org.junit.Test

class AudioDeviceManagerTest {

    @Test
    fun `enableBluetoothSco should start a bluetooth SCO connection on a delayed background thread`() {
        val asyncTaskWrapper = mock<AsyncTaskWrapper> {
            whenever(mock.execute(any())).thenAnswer {
                (it.arguments[0] as () -> Unit).invoke()
            }
        }
        val audioManager = mock<AudioManager>()
        val threadWrapper = mock<ThreadWrapper>()
        val audioDeviceManager = AudioDeviceManager(mock(), mock(), audioManager, mock(), mock(),
                asyncTaskWrapper, threadWrapper)

        audioDeviceManager.enableBluetoothSco(true)

        verify(audioManager).startBluetoothSco()
        verify(threadWrapper).sleep(1000)
    }

    @Test
    fun `enableBluetoothSco should stop a bluetooth SCO connection`() {
        val asyncTaskWrapper = mock<AsyncTaskWrapper>()
        val audioManager = mock<AudioManager>()
        val threadWrapper = mock<ThreadWrapper>()
        val audioDeviceManager = AudioDeviceManager(mock(), mock(), audioManager, mock(), mock(),
                asyncTaskWrapper, threadWrapper)

        audioDeviceManager.enableBluetoothSco(false)

        verify(audioManager).stopBluetoothSco()
        verifyZeroInteractions(asyncTaskWrapper)
        verifyZeroInteractions(threadWrapper)
    }
}
