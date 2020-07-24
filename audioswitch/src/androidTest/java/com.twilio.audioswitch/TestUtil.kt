package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.DEVICE_NAME
import com.twilio.audioswitch.android.FakeBluetoothIntentProcessor
import com.twilio.audioswitch.android.HEADSET_NAME
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetCacheManager
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetState
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.audioswitch.selection.AudioFocusRequestWrapper
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

val TAG = "TestUtil"

internal fun setupFakeAudioDeviceSelector(context: Context):
        Pair<AudioDeviceSelector, BluetoothHeadsetReceiver> {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val logger = LogWrapper()
    val audioDeviceManager =
            AudioDeviceManager(context,
                    logger,
                    audioManager,
                    BuildWrapper(),
                    AudioFocusRequestWrapper())
    val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    val bluetoothIntentProcessor = FakeBluetoothIntentProcessor()
    val deviceCache = BluetoothHeadsetCacheManager(logger)
    val headsetState = BluetoothHeadsetState(logger)
    val bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger, bluetoothIntentProcessor, audioDeviceManager, deviceCache, headsetState)
    val bluetoothController = BluetoothAdapter.getDefaultAdapter()?.let { bluetoothAdapter ->
        BluetoothController(context,
                bluetoothAdapter,
                BluetoothHeadsetManager(logger, bluetoothAdapter, deviceCache, headsetState),
                bluetoothHeadsetReceiver)
    } ?: run {
        null
    }
    return Pair(AudioDeviceSelector(logger,
            audioDeviceManager,
            wiredHeadsetReceiver,
            bluetoothController,
            deviceCache,
            headsetState),
            bluetoothHeadsetReceiver)
}

internal fun simulateBluetoothSystemIntent(
    context: Context,
    bluetoothHeadsetReceiver: BluetoothHeadsetReceiver,
    deviceName: String = HEADSET_NAME,
    action: String = BluetoothDevice.ACTION_ACL_CONNECTED
) {
    val intent = Intent(action).apply {
        putExtra(DEVICE_NAME, deviceName)
    }
    bluetoothHeadsetReceiver.onReceive(context, intent)
}

fun getTargetContext(): Context = getInstrumentation().targetContext

fun getInstrumentationContext(): Context = getInstrumentation().context

fun isSpeakerPhoneOn() =
        (getTargetContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?)?.let {
            it.isSpeakerphoneOn
        } ?: false
