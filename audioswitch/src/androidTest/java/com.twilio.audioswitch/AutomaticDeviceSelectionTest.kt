package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.audioswitch.AudioDevice.Earpiece
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AutomaticDeviceSelectionTest : AndroidTestBase() {

    @UiThreadTest
    @Test
    fun `it_should_select_the_bluetooth_audio_device_by_default`() {
        val context = getInstrumentationContext()
        val (audioSwitch, bluetoothHeadsetReceiver, wiredHeadsetReceiver) = setupFakeAudioSwitch(context)

        audioSwitch.start { _, _ -> }
        simulateBluetoothSystemIntent(context, bluetoothHeadsetReceiver)
        simulateWiredHeadsetSystemIntent(context, wiredHeadsetReceiver)

        assertThat(audioSwitch.selectedAudioDevice!! is AudioDevice.BluetoothHeadset, equalTo(true))
        audioSwitch.stop()
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_wired_headset_by_default`() {
        val context = getInstrumentationContext()
        val (audioSwitch, bluetoothHeadsetReceiver, wiredHeadsetReceiver) =
            setupFakeAudioSwitch(context, listOf(WiredHeadset::class.java))

        audioSwitch.start { _, _ -> }
        simulateBluetoothSystemIntent(context, bluetoothHeadsetReceiver)
        simulateWiredHeadsetSystemIntent(context, wiredHeadsetReceiver)

        assertThat(audioSwitch.selectedAudioDevice!! is WiredHeadset, equalTo(true))
        audioSwitch.stop()
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_earpiece_audio_device_by_default`() {
        val context = getInstrumentationContext()
        val (audioSwitch, bluetoothHeadsetReceiver) =
            setupFakeAudioSwitch(context, listOf(Earpiece::class.java))
        audioSwitch.start { _, _ -> }
        simulateBluetoothSystemIntent(context, bluetoothHeadsetReceiver)

        assertThat(audioSwitch.selectedAudioDevice!! is Earpiece, equalTo(true))
        audioSwitch.stop()
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_speakerphone_audio_device_by_default`() {
        val context = getInstrumentationContext()
        val (audioSwitch, bluetoothHeadsetReceiver) =
            setupFakeAudioSwitch(context, listOf(Speakerphone::class.java))
        audioSwitch.start { _, _ -> }
        simulateBluetoothSystemIntent(context, bluetoothHeadsetReceiver)

        assertThat(audioSwitch.selectedAudioDevice!! is Speakerphone, equalTo(true))
        audioSwitch.stop()
    }
}
