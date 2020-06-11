package com.twilio.audioswitch

import android.media.AudioManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

fun setupAudioManagerMock() =
        mock<AudioManager> {
            whenever(mock.mode).thenReturn(AudioManager.MODE_NORMAL)
            whenever(mock.isMicrophoneMute).thenReturn(true)
            whenever(mock.isSpeakerphoneOn).thenReturn(true)
            whenever(mock.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(emptyArray())
        }
