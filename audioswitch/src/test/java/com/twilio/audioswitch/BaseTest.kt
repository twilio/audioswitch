package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.AudioManager.OnAudioFocusChangeListener
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

open class BaseTest {
    private val bluetoothClass = mock<BluetoothClass> {
        whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE)
    }
    internal val expectedBluetoothDevice = mock<BluetoothDevice> {
        whenever(mock.name).thenReturn(DEVICE_NAME)
        whenever(mock.bluetoothClass).thenReturn(bluetoothClass)
    }
    internal val context = mock<Context>()
    internal val logger = mock<Logger>()
    internal val audioManager = setupAudioManagerMock()
    internal val bluetoothAdapter = mock<BluetoothAdapter>()
    internal val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
    internal val buildWrapper = mock<BuildWrapper>()
    internal val audioFocusRequest = mock<AudioFocusRequestWrapper>()
    internal val defaultAudioFocusChangeListener = mock<OnAudioFocusChangeListener>()
    internal val audioDeviceManager = AudioDeviceManager(context, logger, audioManager, buildWrapper,
            audioFocusRequest, defaultAudioFocusChangeListener)
    internal val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    internal var handler = setupScoHandlerMock()
    internal var systemClockWrapper = setupSystemClockMock()
    internal val headsetProxy = mock<BluetoothHeadset>()
    internal var headsetManager =
        BluetoothHeadsetManager(
            context, logger, bluetoothAdapter,
            audioDeviceManager, bluetoothScoHandler = handler,
            systemClockWrapper = systemClockWrapper, headsetProxy = headsetProxy
        )
    internal var audioSwitch = AudioSwitch(
        context = context,
        logger = logger,
        audioDeviceManager = audioDeviceManager,
        wiredHeadsetReceiver = wiredHeadsetReceiver,
        headsetManager = headsetManager,
        audioFocusChangeListener = defaultAudioFocusChangeListener
    )
}
