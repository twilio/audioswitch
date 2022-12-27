package com.twilio.audioswitch

import com.twilio.audioswitch.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.AudioDevice.Earpiece
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset
import com.twilio.audioswitch.TestType.BluetoothHeadsetTest
import com.twilio.audioswitch.TestType.EarpieceAndSpeakerTest
import com.twilio.audioswitch.TestType.WiredHeadsetTest

private sealed class TestType {
    object EarpieceAndSpeakerTest : TestType()
    object WiredHeadsetTest : TestType()
    object BluetoothHeadsetTest : TestType()
}

private val commonTestCases = listOf(
    listOf(
        BluetoothHeadset::class.java,
        WiredHeadset::class.java,
        Earpiece::class.java,
        Speakerphone::class.java
    ),
    listOf(
        WiredHeadset::class.java,
        BluetoothHeadset::class.java,
        Earpiece::class.java,
        Speakerphone::class.java
    ),
    listOf(
        WiredHeadset::class.java,
        Earpiece::class.java,
        BluetoothHeadset::class.java,
        Speakerphone::class.java
    ),
    listOf(
        WiredHeadset::class.java,
        Earpiece::class.java,
        Speakerphone::class.java,
        BluetoothHeadset::class.java
    ),
    listOf(
        BluetoothHeadset::class.java,
        Earpiece::class.java,
        WiredHeadset::class.java,
        Speakerphone::class.java
    ),
    listOf(
        BluetoothHeadset::class.java,
        Earpiece::class.java,
        Speakerphone::class.java,
        WiredHeadset::class.java
    ),
    listOf(
        Earpiece::class.java,
        BluetoothHeadset::class.java,
        WiredHeadset::class.java,
        Speakerphone::class.java
    ),
    listOf(
        BluetoothHeadset::class.java,
        WiredHeadset::class.java,
        Speakerphone::class.java,
        Earpiece::class.java
    ),
    listOf(
        Speakerphone::class.java,
        BluetoothHeadset::class.java,
        WiredHeadset::class.java,
        Earpiece::class.java
    ),
    listOf(
        BluetoothHeadset::class.java,
        Speakerphone::class.java,
        WiredHeadset::class.java,
        Earpiece::class.java
    ),
    listOf(
        BluetoothHeadset::class.java,
        Speakerphone::class.java,
        WiredHeadset::class.java
    ),
    listOf(
        Earpiece::class.java,
        BluetoothHeadset::class.java
    ),
    listOf(Speakerphone::class.java),
    listOf()
)

private fun getTestInput(testType: TestType): Array<Any> {
    return mutableListOf<Array<Any>>().apply {
        commonTestCases.forEach { devices ->
            val includeDevices = when (testType) {
                EarpieceAndSpeakerTest -> true
                WiredHeadsetTest -> {
                    val wiredHeadsetIndex = devices.indexOfFirst { it == WiredHeadset::class.java }
                    val notBluetoothNorWiredHeadsetIndex =
                        devices.indexOfFirst { it != WiredHeadset::class.java && it != BluetoothHeadset::class.java }
                    wiredHeadsetIndex != -1 && wiredHeadsetIndex < notBluetoothNorWiredHeadsetIndex
                }
                BluetoothHeadsetTest -> {
                    val bluetoothIndex = devices.indexOfFirst { it == BluetoothHeadset::class.java }
                    val notBluetoothNorWiredHeadsetIndex =
                        devices.indexOfFirst { it != WiredHeadset::class.java && it != BluetoothHeadset::class.java }
                    bluetoothIndex != -1 && bluetoothIndex < notBluetoothNorWiredHeadsetIndex
                }
            }
            if (!includeDevices) {
                return@forEach
            }
            val arrayOf = arrayOf(devices, getExpectedDevice(testType, devices))
            add(arrayOf)
        }
    }.toTypedArray()
}

private fun getExpectedDevice(
    testType: TestType,
    preferredDeviceList: List<Class<out AudioDevice>>
): AudioDevice {
    return when (testType) {
        EarpieceAndSpeakerTest -> {
            preferredDeviceList.find {
                it == Earpiece::class.java || it == Speakerphone::class.java
            }?.newInstance() ?: Earpiece()
        }
        WiredHeadsetTest -> {
            preferredDeviceList.find {
                it == WiredHeadset::class.java || it == Speakerphone::class.java
            }?.newInstance() ?: WiredHeadset()
        }
        BluetoothHeadsetTest -> {
            preferredDeviceList.find {
                it == BluetoothHeadset::class.java || it == Earpiece::class.java || it == Speakerphone::class.java
            }?.newInstance() ?: BluetoothHeadset()
        }
    }
}

class EarpieceAndSpeakerParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            return getTestInput(EarpieceAndSpeakerTest)
        }
    }
}

class WiredHeadsetParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            return getTestInput(WiredHeadsetTest)
        }
    }
}

class BluetoothHeadsetParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            return getTestInput(BluetoothHeadsetTest)
        }
    }
}

class DefaultDeviceParams {
    companion object {
        @JvmStatic
        fun provideParams() = commonTestCases
    }
}
