package com.twilio.audioswitch

/**
 * This class represents a single audio device that has been retrieved by the [AudioSwitch].
 */
sealed class AudioDevice {

    /** The friendly name of the device.*/
    abstract val name: String

    /** An [AudioDevice] representing a Bluetooth Headset.*/
    data class BluetoothHeadset constructor(override val name: String = "Bluetooth") : AudioDevice()

    /** An [AudioDevice] representing a Wired Headset.*/
    data class WiredHeadset constructor(override val name: String = "Wired Headset") : AudioDevice()

    /** An [AudioDevice] representing the Earpiece.*/
    data class Earpiece constructor(override val name: String = "Earpiece") : AudioDevice()

    /** An [AudioDevice] representing the Speakerphone.*/
    data class Speakerphone constructor(override val name: String = "Speakerphone") : AudioDevice()
}
