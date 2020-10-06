package com.twilio.audioswitch.android

import android.bluetooth.BluetoothDevice

internal const val DEFAULT_DEVICE_NAME = "Bluetooth"

internal data class BluetoothDeviceWrapperImpl(
    val device: BluetoothDevice,
    override val name: String = device.name ?: DEFAULT_DEVICE_NAME,
    override val deviceClass: Int? = device.bluetoothClass?.deviceClass
) : BluetoothDeviceWrapper
