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
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.wired.WiredHeadsetReceiver
import java.util.concurrent.TimeoutException

internal fun setupFakeAudioSwitch(context: Context):
        Pair<AudioSwitch, BluetoothHeadsetManager> {

    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val logger = Logger()
    val audioDeviceManager =
            AudioDeviceManager(context,
                    logger,
                    audioManager,
                    BuildWrapper(),
                    AudioFocusRequestWrapper(),
                    AudioManager.OnAudioFocusChangeListener {})
    val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    val headsetManager = BluetoothAdapter.getDefaultAdapter()?.let { bluetoothAdapter ->
        BluetoothHeadsetManager(context, logger, bluetoothAdapter, audioDeviceManager,
                bluetoothIntentProcessor = FakeBluetoothIntentProcessor())
    } ?: run {
        null
    }
    return Pair(AudioSwitch(logger,
        audioDeviceManager,
        wiredHeadsetReceiver,
        headsetManager),
        headsetManager!!)
}

internal fun simulateBluetoothSystemIntent(
    context: Context,
    headsetManager: BluetoothHeadsetManager,
    deviceName: String = HEADSET_NAME,
    action: String = BluetoothDevice.ACTION_ACL_CONNECTED
) {
    val intent = Intent(action).apply {
        putExtra(DEVICE_NAME, deviceName)
    }
    headsetManager.onReceive(context, intent)
}

fun getTargetContext(): Context = getInstrumentation().targetContext

fun getInstrumentationContext(): Context = getInstrumentation().context

fun isSpeakerPhoneOn() =
        (getTargetContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?)?.let {
            it.isSpeakerphoneOn
        } ?: false

fun retryAssertion(
    timeoutInMilliseconds: Long = 10000L,
    assertionAction: () -> Unit
) {
    val startTime = System.currentTimeMillis()
    var currentTime = 0L
    while (currentTime <= timeoutInMilliseconds) {
        try {
            assertionAction()
            return
        } catch (error: AssertionError) {
            currentTime = System.currentTimeMillis() - startTime
            Thread.sleep(10)
        }
    }
    throw TimeoutException("Assertion timeout occurred")
}
