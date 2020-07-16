package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.audioswitch.selection.AudioDevice.Earpiece
import com.twilio.audioswitch.selection.AudioDeviceSelector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AutomaticDeviceSelectionTest {

    @UiThreadTest
    @Test
    fun `it_should_select_the_bluetooth_audio_device_by_default`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(getInstrumentationContext())

        audioDeviceSelector.start { _, _ -> }
        simulateBluetoothSystemIntent(getInstrumentationContext(), bluetoothHeadsetReceiver)

        assertEquals("Fake Headset", audioDeviceSelector.selectedAudioDevice!!.name)
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_earpiece_audio_device_by_default`() {
        val audioDeviceSelector = AudioDeviceSelector(getInstrumentationContext())
        audioDeviceSelector.start { _, _ -> }

        assertThat(audioDeviceSelector.selectedAudioDevice is Earpiece, equalTo(true))
    }
}
