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

    private var cacheManager = BluetoothHeadsetCacheManager(mock())

    @Test
    fun `remove removes an existing headset from the cache`() {
        val headset = createHeadset("Headset")
        cacheManager.add(headset)

        cacheManager.remove(headset)

        assertThat(cacheManager.cachedDevices.isEmpty(), equalTo(true))
    }

    @Test
    fun `clear should remove all of the headsets from the cache`() {
        val headset1 = createHeadset("Headset 1")
        val headset2 = createHeadset("Headset 2")
        cacheManager.add(headset1)
        cacheManager.add(headset2)
        assertThat(cacheManager.cachedDevices.size, equalTo(2))

        cacheManager.clear()

        assertThat(cacheManager.cachedDevices.isEmpty(), equalTo(true))
    }

    private fun createHeadset(name: String): AudioDevice.BluetoothHeadset {
        val device = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn(name)
        }
        return AudioDevice.BluetoothHeadset(
                BluetoothDeviceWrapperImpl(device))
    }
}
