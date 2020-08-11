package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceSelector
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ConnectedBluetoothHeadsetTest {

    private val bluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val previousBluetoothEnabled by lazy { bluetoothAdapter.isEnabled }
    private val context by lazy { getInstrumentationContext() }
    private val audioDeviceSelector by lazy { AudioDeviceSelector(context) }
    private lateinit var bluetoothHeadset: BluetoothHeadset
    private lateinit var expectedBluetoothDevice: AudioDevice.BluetoothHeadset
    private val bluetoothServiceConnected = CountDownLatch(1)
    private val bluetoothServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            bluetoothHeadset = proxy as BluetoothHeadset
            bluetoothServiceConnected.countDown()
        }

        override fun onServiceDisconnected(profile: Int) {
        }
    }
    private val bluetoothHeadsetFilter = IntentFilter().apply {
        addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
    }
    private val bluetoothStateConnected = CountDownLatch(1)
    private val bluetoothAudioStateConnected = CountDownLatch(1)
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED).let { state ->
                when (state) {
                    BluetoothHeadset.STATE_CONNECTED -> {
                        bluetoothStateConnected.countDown()
                    }
                    BluetoothHeadset.STATE_AUDIO_CONNECTED -> {
                        bluetoothAudioStateConnected.countDown()
                    }
                }
            }
        }
    }

    @Before
    fun setup() {
        context.registerReceiver(bluetoothReceiver, bluetoothHeadsetFilter)
        if (!previousBluetoothEnabled) {
            bluetoothAdapter.enable()
        }
        bluetoothAdapter.getProfileProxy(context, bluetoothServiceListener, BluetoothProfile.HEADSET)
        assumeTrue(bluetoothServiceConnected.await(10, TimeUnit.SECONDS))
        if (!previousBluetoothEnabled) {
            assumeTrue(bluetoothStateConnected.await(10, TimeUnit.SECONDS))
        }
        assumeTrue(bluetoothHeadset.connectedDevices.size == 1)
        expectedBluetoothDevice = AudioDevice.BluetoothHeadset(bluetoothHeadset.connectedDevices.first().name)
    }

    @After
    fun teardown() {
        audioDeviceSelector.deactivate()
        audioDeviceSelector.stop()
        if (previousBluetoothEnabled) {
            bluetoothAdapter.enable()
        } else {
            bluetoothAdapter.disable()
        }
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
        context.unregisterReceiver(bluetoothReceiver)
    }

    @Test
    fun it_should_select_bluetooth_device_by_default() {
        val bluetoothDeviceConnected = CountDownLatch(1)
        lateinit var actualBluetoothDevice: AudioDevice

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.start { _, audioDevice ->
                if (audioDevice is AudioDevice.BluetoothHeadset) {
                    actualBluetoothDevice = audioDevice
                    bluetoothDeviceConnected.countDown()
                }
            }
        }

        assertTrue(bluetoothDeviceConnected.await(5, TimeUnit.SECONDS))
        assertEquals(expectedBluetoothDevice, actualBluetoothDevice)
    }

    @Test
    fun it_should_remove_bluetooth_device_after_removed() {
        val bluetoothDeviceConnected = CountDownLatch(1)
        lateinit var actualBluetoothDevice: AudioDevice
        val deviceListenerCallbacks = CountDownLatch(3)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.start { _, audioDevice ->
                deviceListenerCallbacks.countDown()
                if (audioDevice is AudioDevice.BluetoothHeadset) {
                    actualBluetoothDevice = audioDevice
                    bluetoothDeviceConnected.countDown()
                }
            }
        }

        assertTrue(bluetoothDeviceConnected.await(5, TimeUnit.SECONDS))
        assertEquals(expectedBluetoothDevice, actualBluetoothDevice)

        bluetoothAdapter.disable()
        assertTrue(deviceListenerCallbacks.await(5, TimeUnit.SECONDS))
        assertNull(audioDeviceSelector.availableAudioDevices.find { it is AudioDevice.BluetoothHeadset })
        assertFalse(audioDeviceSelector.selectedAudioDevice is AudioDevice.BluetoothHeadset)
    }

    @Test
    fun it_should_allow_selecting_a_bluetooth_device() {
        val bluetoothDeviceConnected = CountDownLatch(1)
        val deviceListenerCallbacks = CountDownLatch(3)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.start { _, audioDevice ->
                deviceListenerCallbacks.countDown()
                if (audioDevice is AudioDevice.BluetoothHeadset) {
                    bluetoothDeviceConnected.countDown()
                }
            }
        }

        assertTrue(bluetoothDeviceConnected.await(5, TimeUnit.SECONDS))

        audioDeviceSelector.selectDevice(audioDeviceSelector.availableAudioDevices.find { it is AudioDevice.BluetoothHeadset })
        assertEquals(expectedBluetoothDevice, audioDeviceSelector.selectedAudioDevice)
    }

    @Test
    fun it_should_allow_selecting_another_audio_device_with_bluetooth_device_connected() {
        val bluetoothDeviceConnected = CountDownLatch(1)
        val deviceListenerCallbacks = CountDownLatch(3)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.start { _, audioDevice ->
                deviceListenerCallbacks.countDown()
                if (audioDevice is AudioDevice.BluetoothHeadset) {
                    bluetoothDeviceConnected.countDown()
                }
            }
        }

        assertTrue(bluetoothDeviceConnected.await(5, TimeUnit.SECONDS))

        val expectedAudioDevice = audioDeviceSelector.availableAudioDevices.find { it !is AudioDevice.BluetoothHeadset }
        audioDeviceSelector.selectDevice(expectedAudioDevice)
        assertEquals(expectedAudioDevice, audioDeviceSelector.selectedAudioDevice)
    }

    @Test
    fun it_should_activate_a_bluetooth_device() {
        val bluetoothDeviceConnected = CountDownLatch(1)
        val deviceListenerCallbacks = CountDownLatch(3)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.start { _, audioDevice ->
                deviceListenerCallbacks.countDown()
                if (audioDevice is AudioDevice.BluetoothHeadset) {
                    bluetoothDeviceConnected.countDown()
                }
            }
        }

        assertTrue(bluetoothDeviceConnected.await(5, TimeUnit.SECONDS))

        audioDeviceSelector.selectDevice(audioDeviceSelector.availableAudioDevices.find { it is AudioDevice.BluetoothHeadset })
        assertEquals(expectedBluetoothDevice, audioDeviceSelector.selectedAudioDevice)

        assertTrue(bluetoothAudioStateConnected.count > 0)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            audioDeviceSelector.activate()
        }
        assertFalse(isSpeakerPhoneOn())
        assertTrue(bluetoothAudioStateConnected.await(10, TimeUnit.SECONDS))
        assertTrue(bluetoothHeadset.isAudioConnected(bluetoothHeadset.connectedDevices.first()))
    }
}