package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.audioswitch.android.HEADSET_2_NAME
import com.twilio.audioswitch.android.HEADSET_NAME
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
}
