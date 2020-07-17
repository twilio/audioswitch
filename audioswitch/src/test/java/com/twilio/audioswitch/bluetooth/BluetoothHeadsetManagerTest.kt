package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.LogWrapper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BluetoothHeadsetManagerTest {

    private val deviceListener = mock<BluetoothHeadsetConnectionListener>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val deviceCache = BluetoothHeadsetCacheManager(logger)
    private var bluetoothHeadsetManager = BluetoothHeadsetManager(
            logger,
            bluetoothAdapter,
            deviceCache,
            deviceListener)

    @Test
    fun `onServiceConnected should notify the deviceListener`() {
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        deviceListener.onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceConnected should notify the deviceListener with multiple headsets`() {
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice, expectedDevice)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        verify(deviceListener, times(2)).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceConnected should add to the headset cache with multiple headsets`() {
        val device = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val device2 = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test 2")
        }
        val bluetoothDevices = listOf(device, device2)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        assertThat(deviceCache.cachedHeadsets.size, equalTo(2))
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth headsets`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        bluetoothHeadsetManager.headsetListener = null
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        bluetoothHeadsetManager.onServiceDisconnected(0)

        verify(deviceListener).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceDisconnected should not notify the deviceListener if deviceListener is null`() {
        bluetoothHeadsetManager.headsetListener = null
        bluetoothHeadsetManager.onServiceDisconnected(0)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `stop should close profile proxy`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        bluetoothHeadsetManager.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
    }

    @Test
    fun `stop should unassign the deviceListener`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        bluetoothHeadsetManager.stop()

        assertThat(bluetoothHeadsetManager.headsetListener, `is`(nullValue()))
    }

    @Test
    fun `onServiceDisconnected should clear the headset cache`() {
        val device = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val device2 = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test 2")
        }
        val bluetoothDevices = listOf(device, device2)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)
        assertThat(deviceCache.cachedHeadsets.size, equalTo(2))

        bluetoothHeadsetManager.onServiceDisconnected(0)

        assertThat(deviceCache.cachedHeadsets.isEmpty(), equalTo(true))
    }
}
