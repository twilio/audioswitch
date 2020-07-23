package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.twilio.audioswitch.android.BluetoothIntentProcessorImpl
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDeviceChangeListener
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.audioswitch.selection.AudioFocusRequestWrapper
import com.twilio.audioswitch.setupAudioManagerMock
import com.twilio.audioswitch.setupScoHandlerMock
import com.twilio.audioswitch.setupSystemClockMock
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

open class BaseTest {
    internal val context = mock<Context>()
    internal val logger = mock<LogWrapper>()
    internal val audioManager = setupAudioManagerMock()
    internal val bluetoothAdapter = mock<BluetoothAdapter>()
    internal val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
    internal val headsetState = HeadsetState(logger)
    internal var headsetManager = BluetoothHeadsetManager(logger, bluetoothAdapter,
            headsetState)
    internal val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    internal val buildWrapper = mock<BuildWrapper>()
    internal val audioFocusRequest = mock<AudioFocusRequestWrapper>()
    internal val audioDeviceManager = AudioDeviceManager(context,
            logger,
            audioManager,
            buildWrapper,
            audioFocusRequest)
    internal var handler = setupScoHandlerMock()
    internal var systemClockWrapper = setupSystemClockMock()
    internal var bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context,
            logger,
            BluetoothIntentProcessorImpl(),
            audioDeviceManager,
            headsetState,
            headsetManager,
            EnableBluetoothScoJob(logger, audioDeviceManager, headsetState, handler, systemClockWrapper),
            DisableBluetoothScoJob(logger, audioDeviceManager, headsetState, handler, systemClockWrapper))
    internal val bluetoothController = BluetoothController(
            context,
            bluetoothAdapter,
            headsetManager,
            bluetoothHeadsetReceiver)
    internal var audioDeviceSelector = AudioDeviceSelector(
            logger,
            audioDeviceManager,
            wiredHeadsetReceiver,
            bluetoothController,
            headsetState
    )
    internal val bluetoothControllerAssertions = BluetoothControllerAssertions()
}
