package com.twilio.audioswitch

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twilio.audioswitch.selection.AudioDeviceSelector
import junit.framework.Assert.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioDeviceSelectorTest {
    @Test
    fun `it_should_return_valid_semver_formatted_version`() {
        val semVerRegex = Regex("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$")
        val version: String = AudioDeviceSelector.VERSION
        assertNotNull(version)
        assertTrue(version.matches(semVerRegex))
    }
}
