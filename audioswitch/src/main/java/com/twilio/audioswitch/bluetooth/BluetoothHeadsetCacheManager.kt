package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset

private const val TAG = "BluetoothDeviceCacheManager"

internal class BluetoothHeadsetCacheManager(private val logger: LogWrapper) {

    private val mutableCachedHeadsets = mutableSetOf<BluetoothHeadset>()
    val cachedHeadsets: Set<BluetoothHeadset> get() = mutableCachedHeadsets

    fun add(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedHeadsets.add(bluetoothHeadset)
        if (result) logger.d(TAG, "Added a new bluetooth headset to the cache: ${bluetoothHeadset.name}")
    }

    fun remove(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedHeadsets.remove(bluetoothHeadset)
        if (result) logger.d(TAG, "Removed a bluetooth headset from the cache: ${bluetoothHeadset.name}")
    }

    fun getActiveHeadset() = cachedHeadsets.firstOrNull()

    fun clear() {
        mutableCachedHeadsets.clear()
        logger.d(TAG, "Cleared the headset cache")
    }
}
