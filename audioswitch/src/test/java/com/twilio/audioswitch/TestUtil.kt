package com.twilio.audioswitch

import android.media.AudioManager
import android.os.Handler
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.bluetooth.BluetoothScoJob
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat

internal fun setupAudioManagerMock() =
        mock<AudioManager> {
            whenever(mock.mode).thenReturn(AudioManager.MODE_NORMAL)
            whenever(mock.isMicrophoneMute).thenReturn(true)
            whenever(mock.isSpeakerphoneOn).thenReturn(true)
            whenever(mock.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(emptyArray())
        }

internal fun assertScoJobIsCanceled(handler: Handler, scoJob: BluetoothScoJob) {
    verify(handler).removeCallbacks(isA())
    assertThat(scoJob.bluetoothScoRunnable, `is`(nullValue()))
}
