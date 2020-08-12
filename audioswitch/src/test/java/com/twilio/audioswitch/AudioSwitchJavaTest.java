package com.twilio.audioswitch;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.content.pm.PackageManager;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AudioSwitchJavaTest extends BaseTest {
    private AudioSwitch javaAudioSwitch;
    @Mock PackageManager packageManager;

    @Before
    public void setUp() {
        when(packageManager.hasSystemFeature(any())).thenReturn(true);
        when(getContext$audioswitch_debug().getPackageManager()).thenReturn(packageManager);
        javaAudioSwitch =
                new AudioSwitch(
                        getLogger$audioswitch_debug(),
                        getAudioDeviceManager$audioswitch_debug(),
                        getWiredHeadsetReceiver$audioswitch_debug(),
                        getHeadsetManager$audioswitch_debug());
    }

    @Test
    public void shouldAllowConstruction() {
        assertNotNull(javaAudioSwitch);
    }

    @Test
    public void shouldAllowStart() {
        Function2<List<? extends AudioDevice>, AudioDevice, Unit> audioDeviceListener =
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                };

        javaAudioSwitch.start(audioDeviceListener);
    }

    @Test
    public void shouldAllowActivate() {
        startAudioSwitch();

        javaAudioSwitch.activate();
    }

    @Test
    public void shouldAllowDeactivate() {
        javaAudioSwitch.deactivate();
    }

    @Test
    public void shouldAllowStop() {
        javaAudioSwitch.stop();
    }

    @Test
    public void shouldAllowGettingAvailableDevices() {
        startAudioSwitch();
        List<AudioDevice> availableDevices = javaAudioSwitch.getAvailableAudioDevices();

        assertFalse(availableDevices.isEmpty());
    }

    @Test
    public void shouldAllowGettingSelectedAudioDevice() {
        startAudioSwitch();
        AudioDevice audioDevice = javaAudioSwitch.getSelectedAudioDevice();

        assertNotNull(audioDevice);
    }

    @Test
    public void shouldAllowSelectingAudioDevice() {
        AudioDevice.Earpiece earpiece = new AudioDevice.Earpiece();
        javaAudioSwitch.selectDevice(earpiece);

        assertEquals(earpiece, javaAudioSwitch.getSelectedAudioDevice());
    }

    @Test
    public void shouldDisableLoggingByDefault() {
        assertFalse(javaAudioSwitch.getLoggingEnabled());
    }

    @Test
    public void shouldAllowEnablingLogging() {
        javaAudioSwitch.setLoggingEnabled(true);
    }

    @Test
    public void getVersion_shouldReturnValidSemverFormattedVersion() {
        String semVerRegex =
                "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-"
                        + "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";

        assertNotNull(AudioSwitch.VERSION);
        assertTrue(AudioSwitch.VERSION.matches(semVerRegex));
    }

    private void startAudioSwitch() {
        Function2<List<? extends AudioDevice>, AudioDevice, Unit> audioDeviceListener =
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                };
        javaAudioSwitch.start(
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                });
    }
}
