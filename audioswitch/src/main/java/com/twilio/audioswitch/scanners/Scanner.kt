package com.twilio.audioswitch.scanners

import com.twilio.audioswitch.AudioDevice

interface Scanner {
    fun isDeviceActive(audioDevice: AudioDevice): Boolean
    fun isDeviceInactive(audioDevice: AudioDevice): Boolean =
        this.isDeviceActive(audioDevice).not()

    fun start(listener: Listener): Boolean
    fun start(
        onDeviceConnected: (audioDevice: AudioDevice) -> Unit,
        onDeviceDisconnected: (audioDevice: AudioDevice) -> Unit
    ): Boolean =
        this.start(object : Listener {
            override fun onDeviceConnected(audioDevice: AudioDevice) {
                onDeviceConnected(audioDevice)
            }

            override fun onDeviceDisconnected(audioDevice: AudioDevice) {
                onDeviceDisconnected(audioDevice)
            }
        })

    fun stop(): Boolean

    interface Listener {
        fun onDeviceConnected(audioDevice: AudioDevice)
        fun onDeviceDisconnected(audioDevice: AudioDevice)
    }
}