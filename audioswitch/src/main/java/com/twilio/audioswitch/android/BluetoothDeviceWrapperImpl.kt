package com.twilio.audioswitch.android

import android.bluetooth.BluetoothDevice

internal const val GENERIC_DEVICE_NAME = "Bluetooth"

internal class BluetoothDeviceWrapperImpl(
    private val device: BluetoothDevice,
    override val name: String = device.name ?: GENERIC_DEVICE_NAME,
    override val deviceClass: Int? = device.bluetoothClass?.deviceClass
) : BluetoothDeviceWrapper