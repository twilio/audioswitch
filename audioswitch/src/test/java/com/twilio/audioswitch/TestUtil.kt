package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BaseTest
import com.twilio.audioswitch.bluetooth.BluetoothScoJob
import com.twilio.audioswitch.selection.AudioDevice
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat

const val DEVICE_NAME = "Bluetooth"

internal fun setupAudioManagerMock() =
        mock<AudioManager> {
            whenever(mock.mode).thenReturn(AudioManager.MODE_NORMAL)
            whenever(mock.isMicrophoneMute).thenReturn(true)
            whenever(mock.isSpeakerphoneOn).thenReturn(true)
            whenever(mock.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(emptyArray())
        }

internal fun setupScoHandlerMock() =
        mock<Handler> {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoJob.BluetoothScoRunnable).run()
                true
            }
        }

internal fun setupSystemClockMock() =
        mock<SystemClockWrapper> {
            whenever(mock.elapsedRealtime()).thenReturn(0)
        }

internal fun BaseTest.assertBluetoothHeadsetSetup() {
    verify(bluetoothAdapter).getProfileProxy(
            context,
            headsetManager,
            BluetoothProfile.HEADSET
    )
    verify(context, times(3)).registerReceiver(eq(headsetManager), isA())
}

internal fun BaseTest.assertBluetoothHeadsetTeardown() {
    assertThat(headsetManager.headsetListener, `is`(nullValue()))
    verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    verify(context).unregisterReceiver(headsetManager)
}

internal fun BaseTest.simulateNewBluetoothHeadsetConnection(
    bluetoothDevice: BluetoothDevice = expectedBluetoothDevice
) {
    val intent = mock<Intent> {
        whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
        whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(bluetoothDevice)
    }
    headsetManager.onReceive(context, intent)
}

fun createHeadset(name: String): AudioDevice.BluetoothHeadset = AudioDevice.BluetoothHeadset(name)
