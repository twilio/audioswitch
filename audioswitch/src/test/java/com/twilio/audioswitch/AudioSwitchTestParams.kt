package com.twilio.audioswitch

import com.twilio.audioswitch.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.AudioDevice.Earpiece
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset

private val commonTestCases = listOf(
        listOf(
                BluetoothHeadset::class.java,
                WiredHeadset::class.java,
                Earpiece::class.java,
                Speakerphone::class.java),
        listOf(
                WiredHeadset::class.java,
                BluetoothHeadset::class.java,
                Earpiece::class.java,
                Speakerphone::class.java),
        listOf(
                WiredHeadset::class.java,
                Earpiece::class.java,
                BluetoothHeadset::class.java,
                Speakerphone::class.java),
        listOf(
                WiredHeadset::class.java,
                Earpiece::class.java,
                Speakerphone::class.java,
                BluetoothHeadset::class.java),
        listOf(
                BluetoothHeadset::class.java,
                Earpiece::class.java,
                WiredHeadset::class.java,
                Speakerphone::class.java),
        listOf(
                BluetoothHeadset::class.java,
                Earpiece::class.java,
                Speakerphone::class.java,
                WiredHeadset::class.java),
        listOf(
                Earpiece::class.java,
                BluetoothHeadset::class.java,
                WiredHeadset::class.java,
                Speakerphone::class.java),
        listOf(
                BluetoothHeadset::class.java,
                WiredHeadset::class.java,
                Speakerphone::class.java,
                Earpiece::class.java),
        listOf(
                Speakerphone::class.java,
                BluetoothHeadset::class.java,
                WiredHeadset::class.java,
                Earpiece::class.java),
        listOf(
                BluetoothHeadset::class.java,
                Speakerphone::class.java,
                WiredHeadset::class.java,
                Earpiece::class.java),
        listOf(
                BluetoothHeadset::class.java,
                Speakerphone::class.java,
                WiredHeadset::class.java),
        listOf(
                Earpiece::class.java,
                BluetoothHeadset::class.java),
        listOf(Speakerphone::class.java),
        listOf()
)

private fun buildParamsWithExpectedDevice(expectedDevices: List<AudioDevice>): Array<Any> {
    return mutableListOf<Array<Any>>().apply {
        commonTestCases.forEachIndexed { index, devices ->
            add(arrayOf(devices, expectedDevices[index]))
        }
    }.toTypedArray()
}

private fun buildParamsWithExpectedDeviceAndVerificationCount(expectedDevices: List<AudioDevice>, speakerPhoneVerificationParams: List<Int>): Array<Any> {
    return mutableListOf<Array<Any>>().apply {
        commonTestCases.forEachIndexed { index, devices ->
            add(arrayOf(devices, expectedDevices[index], speakerPhoneVerificationParams[index]))
        }
    }.toTypedArray()
}

class EarpieceSpeakerParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            val expectedDevices = listOf(
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Speakerphone(),
                    Speakerphone(),
                    Speakerphone(),
                    Speakerphone(),
                    Earpiece(),
                    Speakerphone(),
                    Earpiece()
            )
            return buildParamsWithExpectedDevice(expectedDevices)
        }
    }
}

class SpeakerParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            val expectedDevices = listOf(
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Earpiece(),
                    Speakerphone(),
                    Speakerphone(),
                    Speakerphone(),
                    Speakerphone(),
                    Earpiece(),
                    Speakerphone(),
                    Earpiece()
            )
            return buildParamsWithExpectedDevice(expectedDevices)
        }
    }
}

class WiredHeadsetParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            val expectedDevices = listOf(
                    WiredHeadset(),
                    WiredHeadset(),
                    WiredHeadset(),
                    WiredHeadset(),
                    WiredHeadset(),
                    Speakerphone(),
                    WiredHeadset(),
                    WiredHeadset(),
                    Speakerphone(),
                    Speakerphone(),
                    Speakerphone(),
                    WiredHeadset(),
                    Speakerphone(),
                    WiredHeadset()
            )
            val speakerPhoneVerificationParams = listOf(
                    2,
                    2,
                    2,
                    2,
                    2,
                    1,
                    2,
                    1,
                    2,
                    2,
                    2,
                    2,
                    2,
                    2
            )
            return buildParamsWithExpectedDeviceAndVerificationCount(expectedDevices, speakerPhoneVerificationParams)
        }
    }
}

class BluetoothHeadsetParams {
    companion object {
        @JvmStatic
        fun provideParams(): Array<Any> {
            val expectedDevices = listOf(
                    BluetoothHeadset(),
                    BluetoothHeadset(),
                    Earpiece(),
                    Earpiece(),
                    BluetoothHeadset(),
                    BluetoothHeadset(),
                    Earpiece(),
                    BluetoothHeadset(),
                    Speakerphone(),
                    BluetoothHeadset(),
                    BluetoothHeadset(),
                    Earpiece(),
                    Speakerphone(),
                    BluetoothHeadset()
            )
            val speakerPhoneVerificationParams = listOf(
                    2,
                    2,
                    2,
                    2,
                    2,
                    2,
                    2,
                    1,
                    2,
                    1,
                    1,
                    2,
                    2,
                    2
            )
            return buildParamsWithExpectedDeviceAndVerificationCount(expectedDevices, speakerPhoneVerificationParams)
        }
    }
}

class DefaultDeviceParams {
    companion object {
        @JvmStatic
        fun provideParams() = commonTestCases
    }
}
