package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothDevice
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.selection.AudioDevice
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class BluetoothHeadsetCacheManagerTest {

    @Test
    fun `removingDevice removes an existing device from the cache`() {
        val cacheManager = BluetoothHeadsetCacheManager(mock())
        val bluetoothDeviceMock = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Headset 1")
        }
        cacheManager.addDevice(AudioDevice.BluetoothHeadset(
                BluetoothDeviceWrapperImpl(bluetoothDeviceMock)))

        cacheManager.removeDevice(AudioDevice.BluetoothHeadset(
                BluetoothDeviceWrapperImpl(bluetoothDeviceMock)))

        assertThat(cacheManager.cachedDevices.isEmpty(), equalTo(true))
    }
}
