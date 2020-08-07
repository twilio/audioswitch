package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceSelector
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioDeviceSelectorTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    @UiThreadTest
    fun `it_should_disable_logging_by_default`() {
        val audioDeviceSelector = AudioDeviceSelector(context)

        assertFalse(audioDeviceSelector.loggingEnabled)
    }

    @Test
    @UiThreadTest
    fun `it_should_allow_enabling_logging`() {
        val audioDeviceSelector = AudioDeviceSelector(context)

        audioDeviceSelector.loggingEnabled = true

        assertTrue(audioDeviceSelector.loggingEnabled)
    }

    @Test
    @UiThreadTest
    fun `it_should_allow_toggling_logging_while_in_use`() {
        val audioDeviceSelector = AudioDeviceSelector(context)
        audioDeviceSelector.loggingEnabled = true
        assertTrue(audioDeviceSelector.loggingEnabled)
        audioDeviceSelector.start { _, _ -> }
        val earpiece = audioDeviceSelector.availableAudioDevices
            .find { it is AudioDevice.Earpiece }
        assertNotNull(earpiece)
        audioDeviceSelector.selectDevice(earpiece!!)
        assertEquals(earpiece, audioDeviceSelector.selectedAudioDevice)
        audioDeviceSelector.stop()

        audioDeviceSelector.loggingEnabled = false
        assertFalse(audioDeviceSelector.loggingEnabled)

        audioDeviceSelector.start { _, _ -> }
        audioDeviceSelector.stop()
    }
}