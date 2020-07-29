package com.twilio.audioswitch.selection;

import com.twilio.audioswitch.BaseTest;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class AudioDeviceSelectorJavaTest extends BaseTest {
    private AudioDeviceSelector javaAudioDeviceSelector;

    @Before
    public void setUp() {
        javaAudioDeviceSelector = new AudioDeviceSelector(getLogger$audioswitch_debug(),
                getAudioDeviceManager$audioswitch_debug(),
                getWiredHeadsetReceiver$audioswitch_debug(),
                getHeadsetManager$audioswitch_debug());
    }

    @Test
    public void shouldAllowConstruction() {
        assertNotNull(javaAudioDeviceSelector);
    }
}
