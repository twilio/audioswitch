package com.twilio.audioswitch.bluetooth

internal interface BluetoothHeadsetConnectionListener {
    fun onBluetoothHeadsetStateChanged()
    fun onBluetoothHeadsetActivationError()
}
