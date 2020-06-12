package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.AudioManagerWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BluetoothDeviceConnectionListener.ConnectionError.SCO_DISCONNECTION_ERROR

internal class DisableBluetoothScoJob(
    logger: LogWrapper,
    private val audioManagerWrapper: AudioManagerWrapper,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override val scoAction = {
        audioManagerWrapper.enableBluetoothSco(false)
    }

    override val timeoutError = SCO_DISCONNECTION_ERROR
}
