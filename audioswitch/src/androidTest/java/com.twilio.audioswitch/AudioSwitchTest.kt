package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioSwitchTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    @UiThreadTest
    fun it_should_disable_logging_by_default() {
        val audioSwitch = AudioSwitch(context)

        assertFalse(audioSwitch.loggingEnabled)
    }

    @Test
    @UiThreadTest
    fun it_should_allow_enabling_logging() {
        val audioSwitch = AudioSwitch(context)

        audioSwitch.loggingEnabled = true

        assertTrue(audioSwitch.loggingEnabled)
    }

    @Test
    @UiThreadTest
    fun it_should_allow_enabling_logging_at_construction() {
        val audioSwitch = AudioSwitch(context, loggingEnabled = true)

        assertTrue(audioSwitch.loggingEnabled)
    }

    @Test
    @UiThreadTest
    fun it_should_allow_toggling_logging_while_in_use() {
        val audioSwitch = AudioSwitch(context)
        audioSwitch.loggingEnabled = true
        assertTrue(audioSwitch.loggingEnabled)
        audioSwitch.start { _, _ -> }
        val earpiece = audioSwitch.availableAudioDevices
            .find { it is AudioDevice.Earpiece }
        assertNotNull(earpiece)
        audioSwitch.selectDevice(earpiece!!)
        assertEquals(earpiece, audioSwitch.selectedAudioDevice)
        audioSwitch.stop()

        audioSwitch.loggingEnabled = false
        assertFalse(audioSwitch.loggingEnabled)

        audioSwitch.start { _, _ -> }
        audioSwitch.stop()
    }

    @Test
    @UiThreadTest
    fun `it_should_return_valid_semver_formatted_version`() {
        val semVerRegex = Regex("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$")
        val version: String = AudioSwitch.VERSION
        assertNotNull(version)
        assertTrue(version.matches(semVerRegex))
    }
}
