package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class BluetoothControllerTest : BaseTest() {

    @Test
    fun `start should register bluetooth listeners`() {
        val deviceListener = mock<BluetoothHeadsetConnectionListener>()
        bluetoothController.start(deviceListener)

        bluetoothControllerAssertions.assertStart(
                context,
                headsetManager,
                bluetoothHeadsetReceiver,
                deviceListener,
                bluetoothAdapter)
    }

    @Test
    fun `stop should successfully close resources`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        headsetManager.onServiceConnected(0, bluetoothProfile)

        bluetoothController.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
        verify(context).unregisterReceiver(bluetoothHeadsetReceiver)
    }

    @Test
    fun `activate should start bluetooth device audio routing`() {
        bluetoothController.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        bluetoothController.deactivate()

        verify(audioManager).stopBluetoothSco()
    }
}
