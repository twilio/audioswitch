package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.selection.AudioDeviceManager

internal class DisableBluetoothScoJob(
    logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override val scoAction = {
        audioDeviceManager.enableBluetoothSco(false)
    }
}
