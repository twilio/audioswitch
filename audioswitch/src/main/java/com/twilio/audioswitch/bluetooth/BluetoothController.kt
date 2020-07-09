package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager

internal class BluetoothController internal constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothHeadsetManager: BluetoothHeadsetManager,
    private val bluetoothHeadsetReceiver: BluetoothHeadsetReceiver
) {

    fun start(deviceListener: BluetoothDeviceConnectionListener) {
        bluetoothHeadsetManager.deviceListener = deviceListener
        bluetoothHeadsetReceiver.setupDeviceListener(deviceListener)

        bluetoothAdapter.getProfileProxy(
                context,
                bluetoothHeadsetManager,
                BluetoothProfile.HEADSET)

        context.run {
            registerReceiver(
                    bluetoothHeadsetReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            registerReceiver(
                    bluetoothHeadsetReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            registerReceiver(
                    bluetoothHeadsetReceiver,
                    IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
        }
    }

    fun stop() {
        bluetoothHeadsetManager.stop()
        bluetoothHeadsetReceiver.stop()
    }

    fun activate() {
        bluetoothHeadsetReceiver.enableBluetoothSco(true)
    }

    fun deactivate() {
        bluetoothHeadsetReceiver.enableBluetoothSco(false)
    }
}
