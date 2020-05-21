package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED
import android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
import android.media.AudioManager.EXTRA_SCO_AUDIO_STATE
import android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_ERROR
import com.twilio.audioswitch.android.BluetoothDeviceWrapper
import com.twilio.audioswitch.android.BluetoothIntentProcessor
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "BluetoothHeadsetReceiver"

internal class BluetoothHeadsetReceiver(
        private val context: Context,
        private val logger: LogWrapper,
        private val bluetoothIntentProcessor: BluetoothIntentProcessor,
        private val headsetManager: BluetoothHeadsetManager,
        var deviceListener: BluetoothDeviceConnectionListener? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.action?.let { action ->
            logger.d(TAG, "Bluetooth action: $action")
            when (action) {
                ACTION_ACL_CONNECTED -> {
                    intent.getHeadsetDevice()?.let { bluetoothDevice ->
                        logger.d(
                                TAG,
                                "Bluetooth ACL device " +
                                        bluetoothDevice.name +
                                        " connected")
                        deviceListener?.onBluetoothConnected(bluetoothDevice)
                    }
                }
                ACTION_ACL_DISCONNECTED -> {
                    intent.getDisconnectedHeadsetDevice()?.let { bluetoothDevice ->
                        logger.d(
                                TAG,
                                "Bluetooth ACL device " +
                                        bluetoothDevice.name +
                                        " disconnected")
                        deviceListener?.onBluetoothDisconnected()
                    }
                }
                ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, SCO_AUDIO_STATE_ERROR).let { state ->
                        when (state) {
                            SCO_AUDIO_STATE_CONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio connected")
                            }
                            SCO_AUDIO_STATE_DISCONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio disconnected")
                            }
                            SCO_AUDIO_STATE_ERROR -> {
                                logger.e(TAG, "Error retrieving Bluetooth SCO Audio state")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun stop() {
        deviceListener = null
        context.unregisterReceiver(this)
    }

    private fun Intent.getHeadsetDevice(): BluetoothDeviceWrapper? =
            bluetoothIntentProcessor.getBluetoothDevice(this)?.let { device ->
                if (headsetManager.isDeviceConnected(device.name))
                    device
                else
                    null
            }

    private fun Intent.getDisconnectedHeadsetDevice(): BluetoothDeviceWrapper? =
            bluetoothIntentProcessor.getBluetoothDevice(this)?.let { device ->
                if (headsetManager.isDeviceDisconnected(device.name))
                    device
                else
                    null
            }
}
