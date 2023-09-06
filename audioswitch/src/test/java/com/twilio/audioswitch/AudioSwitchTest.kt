package com.twilio.audioswitch

import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class AudioSwitchTest : BaseTest() {

    internal val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }

    @Before
    fun setUp() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun `start should transition to the started state if the current state is stopped`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)

        assertThat(audioSwitch.state, equalTo(AbstractAudioSwitch.State.STARTED))
    }

    @Test
    fun `start should cache the default audio devices and the default selected audio device`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)

        audioSwitch.availableAudioDevices.let { audioDevices ->
            assertThat(audioDevices.size, equalTo(2))
            assertThat(audioDevices[0] is AudioDevice.Earpiece, equalTo(true))
            assertThat(audioDevices[1] is AudioDevice.Speakerphone, equalTo(true))
        }
        assertThat(audioSwitch.selectedAudioDevice is AudioDevice.Earpiece, equalTo(true))
    }

    @Test
    fun `start should do nothing if the current state is started`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)

        try {
            audioSwitch.start(audioDeviceChangeListener)
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `start should do nothing if the current state is activated`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.start(audioDeviceChangeListener)

        try {
            audioSwitch.start(audioDeviceChangeListener)
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `stop should transition to the stopped state if the current state is started`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        assertThat(audioSwitch.state, equalTo(AbstractAudioSwitch.State.STOPPED))
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `stop should stop the bluetooth and wired headset listeners if the current state is activated`() {
        TODO("Implement after deactivate tests are complete")
    }

    @Test
    fun `stop should do nothing if the current state is stopped`() {
        val audioSwitch = audioSwitch

        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        try {
            audioSwitch.stop()
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `stop should unassign the audio device change listener`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        assertThat(audioSwitch.audioDeviceChangeListener, `is`(nullValue()))
    }

    @Test
    fun `activate should transition to the activated state if the current state is started`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()

        assertThat(audioSwitch.state, equalTo(AbstractAudioSwitch.State.ACTIVATED))
    }

    @Test
    fun `activate should set audio focus using Android O method if api version is 26`() {
        val audioSwitch = audioSwitch

        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(
            this.audioFocusRequest.buildRequest(
                defaultAudioFocusChangeListener,
            )
        ).thenReturn(audioFocusRequest)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()

        verify(audioManager).requestAudioFocus(audioFocusRequest)
    }

    @Test
    fun `activate should set audio focus using Android O method if api version is 27`() {
        val audioSwitch = audioSwitch

        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O_MR1)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(
            this.audioFocusRequest.buildRequest(
                defaultAudioFocusChangeListener,
            )
        ).thenReturn(audioFocusRequest)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()

        verify(audioManager).requestAudioFocus(audioFocusRequest)
    }

    @Test
    fun `activate should set audio focus using pre Android O method if api version is 25`() {
        val audioSwitch = audioSwitch
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.N_MR1)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()

        verify(audioManager).requestAudioFocus(
            defaultAudioFocusChangeListener,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
    }

    @Test
    fun `activate should not set audio focus when not managing audio focus`() {
        val audioSwitch = audioSwitch
        audioSwitch.manageAudioFocus = false
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(
            this.audioFocusRequest.buildRequest(
                defaultAudioFocusChangeListener,
            )
        ).thenReturn(audioFocusRequest)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()

        verify(audioManager, never()).requestAudioFocus(audioFocusRequest)
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 26`() {
        val audioSwitch = audioSwitch
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(
            this.audioFocusRequest.buildRequest(
                defaultAudioFocusChangeListener,
            )
        ).thenReturn(audioFocusRequest)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        verify(audioManager).abandonAudioFocusRequest(audioFocusRequest)
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 27`() {
        val audioSwitch = audioSwitch
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O_MR1)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(
            this.audioFocusRequest.buildRequest(
                defaultAudioFocusChangeListener,
            )
        ).thenReturn(audioFocusRequest)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        verify(audioManager).abandonAudioFocusRequest(audioFocusRequest)
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 25`() {
        val audioSwitch = audioSwitch
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.N_MR1)
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        verify(audioManager).abandonAudioFocus(defaultAudioFocusChangeListener)
    }

    @Test
    fun `activate should enable audio routing to the earpiece`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)

        audioSwitch.selectDevice(AudioDevice.Earpiece())
        audioSwitch.activate()

        verify(audioManager).isSpeakerphoneOn = false
    }

    @Test
    fun `activate should enable audio routing to the speakerphone device`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.selectDevice(AudioDevice.Speakerphone())
        audioSwitch.activate()

        verify(audioManager).isSpeakerphoneOn = true
    }

    @Test
    fun `activate should enable audio routing to the wired headset device`() {
        val audioSwitch = audioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.WiredHeadset())
        audioSwitch.activate()

        verify(audioManager).isSpeakerphoneOn = false
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `test stopBluetooth sco scenarios when activating other audio devices`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `activate should do nothing if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `activate should throw an IllegalStateException if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `deactivate should transition to the started state if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `deactivate should do nothing if the current state is stopped`() {
        TODO("Assert cached audio state from activate() -> deactivate")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `deactivate should throw an IllegalStateException if the current state is started`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `selectDevice should throw an IllegalStateException if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `selectDevice should do nothing if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `TODO test all permutations of possible audio devices and their priorities`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `getVersion should return valid semver formatted version`() {
        val semVerRegex = Regex(
            "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                    "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$"
        )
        assertNotNull(AbstractAudioSwitch.VERSION)
        assertTrue(AbstractAudioSwitch.VERSION.matches(semVerRegex))
    }

    @Parameters(source = EarpieceAndSpeakerParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val audioSwitch = AudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = scanner,
            audioManager = audioManager,
            handler = handler,
        )

        audioSwitch.run {
            start(this@AudioSwitchTest.audioDeviceChangeListener)
            activate()
            audioSwitch.onDeviceConnected(expectedDevice)

            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when changing the preferred device list, the correct device should be automatically selected and activated`(
        preferredDeviceList: List<Class<out AudioDevice>>,
    ) {
        val audioSwitch = AudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = setupAudioDeviceScannerMock(),
            audioManager = audioManager,
            handler = handler,
        )

        audioSwitch.run {
            start(this@AudioSwitchTest.audioDeviceChangeListener)
            activate()
            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

            val newPreferredDeviceList = listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Speakerphone::class.java,
                AudioDevice.Earpiece::class.java
            )

            audioSwitch.setPreferredDeviceList(newPreferredDeviceList)
            assertThat(selectedAudioDevice, equalTo(AudioDevice.BluetoothHeadset()))
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when audio mode is not in communication, audio routing should not happen`(
        preferredDeviceList: List<Class<out AudioDevice>>,
    ) {
        val audioDeviceManagerSpy = spy(audioDeviceManager)
        val audioSwitch = AudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManagerSpy,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = setupAudioDeviceScannerMock(),
            audioManager = audioManager,
            handler = handler,
        )

        audioSwitch.run {
            audioMode = AudioManager.MODE_NORMAL
            start(this@AudioSwitchTest.audioDeviceChangeListener)
            activate()
            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

            verify(audioDeviceManagerSpy, never()).enableBluetoothSco(any())
            verify(audioDeviceManagerSpy, never()).enableSpeakerphone(any())
            assertThat(audioSwitch.selectedAudioDevice, nullValue())
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when audio mode is not in communication, audio routing happens when forced`(
        preferredDeviceList: List<Class<out AudioDevice>>,
    ) {
        val audioDeviceManagerSpy = spy(audioDeviceManager)
        val audioSwitch = AudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManagerSpy,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = setupAudioDeviceScannerMock(),
            audioManager = audioManager,
            handler = handler,
        )

        audioSwitch.run {
            audioMode = AudioManager.MODE_NORMAL
            forceHandleAudioRouting = true
            start(this@AudioSwitchTest.audioDeviceChangeListener)
            activate()
            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

            verify(audioDeviceManagerSpy, atLeastOnce()).enableBluetoothSco(any())
            verify(audioDeviceManagerSpy, atLeastOnce()).enableSpeakerphone(any())
            assertThat(audioSwitch.selectedAudioDevice, notNullValue())
        }
    }
}
