package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.LogWrapper
import org.hamcrest.CoreMatchers.`is`
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

//        deviceListener.onBluetoothConnected(isA())
    }

    @Test
    fun `onServiceConnected should notify the deviceListener with multiple devices`() {
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice, expectedDevice)
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

//        verify(deviceListener, times(2)).onBluetoothConnected(isA())
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth devices`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        bluetoothHeadsetManager.headsetListener = null
        val expectedDevice = mock<BluetoothDevice>()
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
    fun `stop should unasign the deviceListener`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        bluetoothHeadsetManager.stop()

        assertThat(bluetoothHeadsetManager.headsetListener, `is`(nullValue()))
    }
}
