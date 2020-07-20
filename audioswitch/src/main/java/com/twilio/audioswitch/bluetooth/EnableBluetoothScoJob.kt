package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.android.SystemClockWrapper
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Activated
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Connected
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Disconnected
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceManager

private const val TAG = "EnableBluetoothScoJob"

internal class EnableBluetoothScoJob(
    private val logger: LogWrapper,
    private val audioDeviceManager: AudioDeviceManager,
    private val headsetCache: BluetoothHeadsetCacheManager,
    private val headsetState: HeadsetState,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper()
) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

    var deviceListener: BluetoothHeadsetConnectionListener? = null
    private var headset: AudioDevice.BluetoothHeadset? = null

    fun executeBluetoothScoJob(headset: AudioDevice.BluetoothHeadset) {
        this.headset = headset
        super.executeBluetoothScoJob()
    }

    override fun scoAction() {
        logger.d(TAG, "Attempting to enable bluetooth SCO")
        audioDeviceManager.enableBluetoothSco(true)
        headsetState.state = Activated
    }

    override fun scoTimeOutAction() {
        headset?.let { headset ->
            headsetCache.remove(headset)
            headsetState.state = if (headsetCache.cachedHeadsets.isEmpty()) {
                Disconnected
            } else {
                Connected
            }
            deviceListener?.onBluetoothHeadsetStateChanged()
        }
    }
}
