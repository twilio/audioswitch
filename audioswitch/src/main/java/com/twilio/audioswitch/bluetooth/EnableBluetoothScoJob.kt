package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.selection.AudioDeviceManager

private const val TAG = "EnableBluetoothScoJob"

internal class EnableBluetoothScoJob(
    private val logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override fun scoAction() {
        logger.d(TAG, "Attempting to enable bluetooth SCO")
        audioDeviceManager.enableBluetoothSco(true)
    }
}
