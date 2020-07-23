package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class BluetoothHeadsetManagerTest : BaseTest() {

    private val headsetListener = mock<BluetoothHeadsetConnectionListener>()
    private val expectedDevice = mock<BluetoothDevice> {
        whenever(mock.name).thenReturn("Test")
    }
    private val bluetoothDevices = listOf(expectedDevice)

    @Before
    fun setUp() {
        headsetManager = BluetoothHeadsetManager(logger, bluetoothAdapter,
                headsetState, headsetListener)
    }

    @Test
    fun `onServiceConnected should notify the deviceListener if there are connected devices`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        headsetManager.onServiceConnected(0, bluetoothProfile)

        verify(headsetListener).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceConnected should set the headset state to Connected if there are connected devices`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        headsetManager.onServiceConnected(0, bluetoothProfile)

        headsetListener.onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        headsetManager.headsetListener = null
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        headsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth headsets`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        headsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        headsetManager.onServiceDisconnected(0)

        verify(headsetListener).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceDisconnected should not notify the deviceListener if deviceListener is null`() {
        headsetManager.headsetListener = null
        headsetManager.onServiceDisconnected(0)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `stop should close profile proxy`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        headsetManager.onServiceConnected(0, bluetoothProfile)

        headsetManager.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
    }

    @Test
    fun `stop should unassign the deviceListener`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        headsetManager.onServiceConnected(0, bluetoothProfile)

        headsetManager.stop()

        assertThat(headsetManager.headsetListener, `is`(nullValue()))
    }
}
