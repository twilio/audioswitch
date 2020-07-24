package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetState.State.Activated
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetState.State.Connected
import com.twilio.audioswitch.selection.AudioDeviceManager

private const val TAG = "DisableBluetoothScoJob"

internal class DisableBluetoothScoJob(
    private val logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    private val bluetoothHeadsetState: BluetoothHeadsetState,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override fun scoAction() {
        logger.d(TAG, "Attempting to disable bluetooth SCO")
        audioDeviceManager.enableBluetoothSco(false)
        bluetoothHeadsetState.state = Connected
    }

    override fun scoTimeOutAction() {
        bluetoothHeadsetState.state = Activated
    }
}
