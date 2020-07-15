package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.twilio.audioswitch.android.BluetoothIntentProcessorImpl
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.selection.AudioFocusRequestWrapper
import com.twilio.audioswitch.setupScoHandlerMock
import com.twilio.audioswitch.setupSystemClockMock
import org.junit.Test

class BluetoothControllerTest {

    private val context = mock<Context>()
    private val audioManager = mock<AudioManager>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val deviceCache = BluetoothHeadsetCacheManager(logger)
    private val bluetoothHeadsetManager = BluetoothHeadsetManager(logger, bluetoothAdapter, deviceCache)
    private val buildWrapper = mock<BuildWrapper>()
    private val audioFocusRequest = mock<AudioFocusRequestWrapper>()
    private val audioDeviceManager = AudioDeviceManager(context,
            logger,
            audioManager,
            buildWrapper,
            audioFocusRequest)
    private var handler = setupScoHandlerMock()
    private var systemClockWrapper = setupSystemClockMock()
    private val deviceListener = mock<BluetoothHeadsetConnectionListener>()
    private var bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(
            context,
            logger,
            BluetoothIntentProcessorImpl(),
            audioDeviceManager,
            deviceCache,
            EnableBluetoothScoJob(logger, audioDeviceManager, deviceCache, handler, systemClockWrapper),
            DisableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper),
            deviceListener)
    private var bluetoothController = BluetoothController(
            context,
            bluetoothAdapter,
            bluetoothHeadsetManager,
            bluetoothHeadsetReceiver)
    private val bluetoothControllerAssertions = BluetoothControllerAssertions()

    @Test
    fun `start should register bluetooth listeners`() {
        val deviceListener = mock<BluetoothHeadsetConnectionListener>()
        bluetoothController.start(deviceListener)

        bluetoothControllerAssertions.assertStart(
                context,
                bluetoothHeadsetManager,
                bluetoothHeadsetReceiver,
                deviceListener,
                bluetoothAdapter)
    }

    @Test
    fun `stop should successfully close resources`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        bluetoothController.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
        verify(context).unregisterReceiver(bluetoothHeadsetReceiver)
    }

    @Test
    fun `activate should start bluetooth device audio routing`() {
        bluetoothController.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        bluetoothController.deactivate()

        verify(audioManager).stopBluetoothSco()
    }
}
