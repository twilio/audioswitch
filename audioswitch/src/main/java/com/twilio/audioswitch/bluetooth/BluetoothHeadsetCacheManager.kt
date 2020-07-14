package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset

private const val TAG = "BluetoothDeviceCacheManager"

internal class BluetoothHeadsetCacheManager(private val logger: LogWrapper) {

    private val mutableCachedDevices = mutableSetOf<BluetoothHeadset>()
    val cachedDevices: Set<BluetoothHeadset> get() = mutableCachedDevices

    fun add(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedDevices.add(bluetoothHeadset)
        if (result) logger.d(TAG, "Added a new bluetooth headset to the cache: ${bluetoothHeadset.name}")
    }

    fun remove(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedDevices.remove(bluetoothHeadset)
        if (result) logger.d(TAG, "Removed a bluetooth headset from the cache: ${bluetoothHeadset.name}")
    }

    fun clear() {
        mutableCachedDevices.clear()
        logger.d(TAG, "Cleared the headset cache")
    }
}
