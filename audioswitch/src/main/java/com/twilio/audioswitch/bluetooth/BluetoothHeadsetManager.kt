package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetEvent.Connect
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetEvent.Disconnect
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetEvent.StartAudioActivation
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetEvent.StartAudioDeactivation
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivating
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivationError
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Connected
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Disconnected
import com.twilio.audioswitch.selection.AudioDevice

private const val TAG = "BluetoothHeadsetManager"

internal class BluetoothHeadsetManager(
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter,
    var headsetListener: BluetoothHeadsetConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    private var headsetProxy: BluetoothHeadset? = null
    private var headsetState: HeadsetState = Disconnected
        set(value) {
            if (field != value) {
                field = value
                logger.d(TAG, "Headset state changed to $field")
            }
        }

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.forEach { device ->
            logger.d(TAG, "Bluetooth " + device.name + " connected")
        }
        if (hasConnectedDevice()) {
            updateState(Connect)
            headsetListener?.onBluetoothHeadsetStateChanged(getHeadsetName())
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        headsetState = Disconnected
        headsetListener?.onBluetoothHeadsetStateChanged()
    }

    fun stop() {
        headsetListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    }

    fun updateState(headsetEvent: HeadsetEvent) {
        when (headsetEvent) {
            Connect -> connect()
            Disconnect -> disconnect()
            is StartAudioActivation -> startAudioActivation(headsetEvent.error)
            is StartAudioDeactivation -> startAudioDeactivation(headsetEvent.error)
            HeadsetEvent.AudioActivated -> audioActivated()
        }
    }

    private fun connect() {
        if (headsetState != AudioActivating) {
            headsetState = Connected
        }
    }

    private fun disconnect() {
        headsetState = when {
            hasActiveHeadset() -> {
                HeadsetState.AudioActivated
            }
            hasConnectedDevice() -> {
                Connected
            }
            else -> {
                Disconnected
            }
        }
    }

    private fun startAudioActivation(error: Boolean = false) {
        headsetState = if (!error) AudioActivating else AudioActivationError
    }

    private fun startAudioDeactivation(error: Boolean = false) {
        headsetState = if (!error) Connected else HeadsetState.AudioActivated
    }

    private fun audioActivated() {
        headsetState = HeadsetState.AudioActivated
    }

    fun hasActiveHeadsetChanged() = headsetState == HeadsetState.AudioActivated && hasConnectedDevice() && hasActiveHeadset()

    fun canActivate() = headsetState == Connected || headsetState == AudioActivationError

    fun canDeactivate() = headsetState == HeadsetState.AudioActivated

    fun hasActivationError() = headsetState == AudioActivationError

    fun getHeadset(bluetoothHeadsetName: String?) =
            if (headsetState != Disconnected) {
                AudioDevice.BluetoothHeadset(bluetoothHeadsetName ?: getHeadsetName()
                ?: "Bluetooth")
            } else null

    private fun getHeadsetName(): String? =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    when {
                        devices.size > 1 && hasActiveHeadset() -> {
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

    private fun hasActiveHeadset() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.any { proxy.isAudioConnected(it) }
                }
            } ?: false

    private fun hasConnectedDevice() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.isNotEmpty()
                }
            } ?: false

    sealed class HeadsetEvent {
        object Connect : HeadsetEvent()
        object Disconnect : HeadsetEvent()
        data class StartAudioActivation(val error: Boolean = false) : HeadsetEvent()
        data class StartAudioDeactivation(val error: Boolean = false) : HeadsetEvent()
        object AudioActivated : HeadsetEvent()
    }

    private sealed class HeadsetState {
        object Disconnected : HeadsetState()
        object Connected : HeadsetState()
        object AudioActivating : HeadsetState()
        object AudioActivationError : HeadsetState()
        object AudioActivated : HeadsetState()
    }
}
