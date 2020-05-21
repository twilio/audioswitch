package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTING
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile.STATE_DISCONNECTING
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "BluetoothHeadsetManager"

internal class BluetoothHeadsetManager(
        private val logger: LogWrapper,
        private val bluetoothAdapter: BluetoothAdapter,
        var deviceListener: BluetoothDeviceConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    private var proxy: BluetoothProfile? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        proxy = bluetoothProfile
        bluetoothProfile.connectedDevices.let { deviceList ->
            deviceList.forEach { device ->
                logger.d(TAG, "Bluetooth " + device.name + " connected")
                deviceListener?.onBluetoothConnected(BluetoothDeviceWrapperImpl(device))
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        deviceListener?.onBluetoothDisconnected()
    }

    fun isDeviceConnected(deviceName: String): Boolean {
        val connectedStates = arrayOf(STATE_CONNECTING, STATE_CONNECTED).toIntArray()
        logger.d(TAG, "Connected Bluetooth devices: ${proxy?.getDevicesMatchingConnectionStates(connectedStates)}")
        return proxy?.getDevicesMatchingConnectionStates(connectedStates)?.let { devices ->
            devices.any { it.name == deviceName }
        } ?: false

    }

    fun isDeviceDisconnected(deviceName: String): Boolean {
        val disconnectedStates = arrayOf(STATE_DISCONNECTING, STATE_DISCONNECTED).toIntArray()
        logger.d(TAG, "Disconnected Bluetooth devices: ${proxy?.getDevicesMatchingConnectionStates(disconnectedStates)}")
        return proxy?.getDevicesMatchingConnectionStates(disconnectedStates)?.let { devices ->
            devices.any { it.name == deviceName }
        } ?: false
    }

    fun stop() {
        deviceListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
    }
}
