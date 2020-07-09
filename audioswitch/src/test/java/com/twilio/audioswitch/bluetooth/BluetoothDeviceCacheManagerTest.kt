package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothDevice
import com.nhaarman.mockitokotlin2.mock
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.selection.AudioDevice
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class BluetoothDeviceCacheManagerTest {

    @Test
    fun `removingDevice removes an existing device from the cache`() {
        val cacheManager = BluetoothDeviceCacheManager(mock())
        val bluetoothDeviceMock = mock<BluetoothDevice>()
        cacheManager.addDevice(AudioDevice.BluetoothHeadset("Headset 1",
                BluetoothDeviceWrapperImpl(bluetoothDeviceMock)))

        cacheManager.removeDevice(AudioDevice.BluetoothHeadset("Headset 1",
                BluetoothDeviceWrapperImpl(bluetoothDeviceMock)))

        assertThat(cacheManager.cachedDevices.isEmpty(), equalTo(true))
    }
}