package com.twilio.audioswitch.android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.Intent
import android.os.Build

internal class BluetoothIntentProcessorImpl : BluetoothIntentProcessor {
    override fun getBluetoothDevice(intent: Intent): BluetoothDeviceWrapper? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
        }?.let { device -> BluetoothDeviceWrapperImpl(device) }
}
