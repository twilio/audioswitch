package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Activated
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Connected
import com.twilio.audioswitch.selection.AudioDeviceManager

private const val TAG = "DisableBluetoothScoJob"

internal class DisableBluetoothScoJob(
    private val logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    private val headsetState: HeadsetState,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override fun scoAction() {
        logger.d(TAG, "Attempting to disable bluetooth SCO")
        audioDeviceManager.enableBluetoothSco(false)
        headsetState.state = Connected
    }

    override fun scoTimeOutAction() {
        headsetState.state = Activated
    }
}
