package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.AudioManagerWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.BluetoothDeviceConnectionListener.ConnectionError.SCO_CONNECTION_ERROR

internal class EnableBluetoothScoJob(
    logger: LogWrapper,
    private val audioManagerWrapper: AudioManagerWrapper,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    override val scoAction = {
        audioManagerWrapper.enableBluetoothSco(true)
    }

    override val timeoutError = SCO_CONNECTION_ERROR
}
