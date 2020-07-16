package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.audioswitch.android.HEADSET_2_NAME
import com.twilio.audioswitch.android.HEADSET_NAME
import com.twilio.audioswitch.selection.AudioDevice
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MultipleBluetoothHeadsetsTest {

    @UiThreadTest
    @Test
    fun `it_should_assert_the_second_bluetooth_headset_when_two_are_connected`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(getInstrumentationContext())

        audioDeviceSelector.start { _, _ -> }
        audioDeviceSelector.activate()
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver)
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver, HEADSET_2_NAME)

        assertThat(audioDeviceSelector.selectedAudioDevice!!.name, equalTo(HEADSET_2_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.first().name, equalTo(HEADSET_2_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.find { it.name == HEADSET_NAME },
                `is`(nullValue()))
        assertThat(isSpeakerPhoneOn(), equalTo(false)) // Best we can do for asserting if a fake BT headset is activated
    }

    @UiThreadTest
    @Test
    fun `it_should_assert_the_second_bluetooth_headset_when_two_are_connected_before_calling_start`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(getInstrumentationContext())

        audioDeviceSelector.start { _, _ -> }
        audioDeviceSelector.activate()
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver)
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver, HEADSET_2_NAME)

        assertThat(audioDeviceSelector.selectedAudioDevice!!.name, equalTo(HEADSET_2_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.first().name, equalTo(HEADSET_2_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.find { it.name == HEADSET_NAME },
                `is`(nullValue()))
        assertThat(isSpeakerPhoneOn(), equalTo(false))
    }

    @UiThreadTest
    @Test
    fun `it_should_assert_the_first_bluetooth_headset_when_two_are_connected_and_the_second_is_disconnected`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(getInstrumentationContext())
        audioDeviceSelector.start { _, _ -> }
        audioDeviceSelector.activate()
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver)
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver, HEADSET_2_NAME)

        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver, HEADSET_2_NAME,
                BluetoothDevice.ACTION_ACL_DISCONNECTED)

        assertThat(audioDeviceSelector.selectedAudioDevice!!.name, equalTo(HEADSET_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.first().name, equalTo(HEADSET_NAME))
        assertThat(audioDeviceSelector.availableAudioDevices.find { it.name == HEADSET_2_NAME },
                `is`(nullValue()))
        assertThat(isSpeakerPhoneOn(), equalTo(false))
    }

    @Test
    fun `it_should_assert_the_second_bluetooth_headset_when_two_are_connected_and_the_first_is_disconnected`() {
        toggleBluetooth(true)
        val connectionLatch = getBluetoothConnectionLatch()
        val cacheClearedLatch = CountDownLatch(1)
        connectionLatch.await(10, TimeUnit.SECONDS)
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(getInstrumentationContext())
        audioDeviceSelector.start { devices, _ ->
            // This boolean condition increments the latch when the headset cache has been cleared
            if (devices.find { it.name == HEADSET_NAME } == null) cacheClearedLatch.count
        }
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver)
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver, HEADSET_2_NAME)

        toggleBluetooth(false)
        cacheClearedLatch.await(10, TimeUnit.SECONDS)

        val earpiece = AudioDevice.Earpiece()
        assertThat(audioDeviceSelector.availableAudioDevices.find { it.name == HEADSET_NAME },
                `is`(nullValue()))
        assertThat(audioDeviceSelector.availableAudioDevices.find { it.name == HEADSET_2_NAME },
                `is`(nullValue()))
        assertThat(audioDeviceSelector.selectedAudioDevice!!.name, equalTo(earpiece.name))
        assertThat(audioDeviceSelector.availableAudioDevices.first().name, equalTo(earpiece.name))
        assertThat(isSpeakerPhoneOn(), equalTo(false))
    }
}
