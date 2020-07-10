package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDevice

private const val TAG = "BluetoothHeadsetManager"

internal class BluetoothHeadsetManager(
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceCache: BluetoothDeviceCacheManager,
    var deviceListener: BluetoothDeviceConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    private var headsetProxy: BluetoothHeadset? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.let { deviceList ->
            deviceList.forEach { device ->
                logger.d(TAG, "Bluetooth " + device.name + " connected")

                val bluetoothHeadset = AudioDevice.BluetoothHeadset(
                        BluetoothDeviceWrapperImpl(device))
                deviceCache.addDevice(bluetoothHeadset)
                deviceListener?.onBluetoothDeviceStateChanged()
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        deviceListener?.onBluetoothDeviceStateChanged()
    }

    fun stop() {
        deviceListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    }
}
