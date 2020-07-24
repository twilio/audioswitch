package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "BluetoothHeadsetManager"

internal class BluetoothHeadsetManager(
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothHeadsetState: BluetoothHeadsetState,
    var headsetListener: BluetoothHeadsetConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    var headsetProxy: BluetoothHeadset? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.forEach { device ->
            logger.d(TAG, "Bluetooth " + device.name + " connected")
        }
        if (hasConnectedDevice()) {
            bluetoothHeadsetState.state = BluetoothHeadsetState.State.Connected
            headsetListener?.onBluetoothHeadsetStateChanged(getHeadsetName())
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        bluetoothHeadsetState.state = BluetoothHeadsetState.State.Disconnected
        headsetListener?.onBluetoothHeadsetStateChanged()
    }

    fun stop() {
        headsetListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    }

    fun hasConnectedDevice() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.isNotEmpty()
                }
            } ?: false

    fun hasActiveDevice() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.any { proxy.isAudioConnected(it) }
                }
            } ?: false

    fun getHeadsetName(): String? =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    when {
                        devices.size > 1 && hasActiveDevice() -> {
                            val device = devices.find { proxy.isAudioConnected(it) }?.name
                            logger.d(TAG, "Device size > 1 with device name: $device")
                            device
                        }
                        devices.size == 1 -> {
                            val device = devices.first().name
                            logger.d(TAG, "Device size 1 with device name: $device")
                            device
                        }
                        else -> {
                            logger.d(TAG, "Device size 0")
                            null
                        }
                    }
                }
            }
}
