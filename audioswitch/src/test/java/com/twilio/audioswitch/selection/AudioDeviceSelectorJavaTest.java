package com.twilio.audioswitch.selection;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.content.pm.PackageManager;
import com.twilio.audioswitch.BaseTest;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AudioDeviceSelectorJavaTest extends BaseTest {
    private AudioDeviceSelector javaAudioDeviceSelector;
    @Mock PackageManager packageManager;

    @Before
    public void setUp() {
        when(packageManager.hasSystemFeature(any())).thenReturn(true);
        when(getContext$audioswitch_debug().getPackageManager()).thenReturn(packageManager);
        javaAudioDeviceSelector =
                new AudioDeviceSelector(
                        getLogger$audioswitch_debug(),
                        getAudioDeviceManager$audioswitch_debug(),
                        getWiredHeadsetReceiver$audioswitch_debug(),
                        getHeadsetManager$audioswitch_debug());
    }

    @Test
    public void shouldAllowConstruction() {
        assertNotNull(javaAudioDeviceSelector);
    }

    @Test
    public void shouldAllowStart() {
        Function2<List<? extends AudioDevice>, AudioDevice, Unit> audioDeviceListener =
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                };

        javaAudioDeviceSelector.start(audioDeviceListener);
    }

    @Test
    public void shouldAllowActivate() {
        startAudioDeviceSelector();

        javaAudioDeviceSelector.activate();
    }

    @Test
    public void shouldAllowDeactivate() {
        javaAudioDeviceSelector.deactivate();
    }

    @Test
    public void shouldAllowStop() {
        javaAudioDeviceSelector.stop();
    }

    @Test
    public void shouldAllowGettingAvailableDevices() {
        startAudioDeviceSelector();
        List<AudioDevice> availableDevices = javaAudioDeviceSelector.getAvailableAudioDevices();

        assertFalse(availableDevices.isEmpty());
    }

    @Test
    public void shouldAllowGettingSelectedAudioDevice() {
        startAudioDeviceSelector();
        AudioDevice audioDevice = javaAudioDeviceSelector.getSelectedAudioDevice();

        assertNotNull(audioDevice);
    }

    @Test
    public void shouldAllowSelectingAudioDevice() {
        AudioDevice.Earpiece earpiece = new AudioDevice.Earpiece();
        javaAudioDeviceSelector.selectDevice(earpiece);

        assertEquals(earpiece, javaAudioDeviceSelector.getSelectedAudioDevice());
    }

    @Test
    public void shouldDisableLoggingByDefault() {
        assertFalse(javaAudioDeviceSelector.getLoggingEnabled());
    }

    @Test
    public void shouldAllowEnablingLogging() {
        javaAudioDeviceSelector.setLoggingEnabled(true);
    }

    private void startAudioDeviceSelector() {
        Function2<List<? extends AudioDevice>, AudioDevice, Unit> audioDeviceListener =
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                };
        javaAudioDeviceSelector.start(
                (audioDevices, audioDevice) -> {
                    assertFalse(audioDevices.isEmpty());
                    assertNotNull(audioDevice);
                    return Unit.INSTANCE;
                });
    }
}
