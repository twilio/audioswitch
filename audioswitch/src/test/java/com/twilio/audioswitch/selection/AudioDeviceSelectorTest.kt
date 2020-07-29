package com.twilio.audioswitch.selection

import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.DEVICE_NAME
import com.twilio.audioswitch.assertBluetoothHeadsetSetup
import com.twilio.audioswitch.assertBluetoothHeadsetTeardown
import com.twilio.audioswitch.BaseTest
import com.twilio.audioswitch.selection.AudioDevice.Earpiece
import com.twilio.audioswitch.selection.AudioDevice.Speakerphone
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.ACTIVATED
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.STARTED
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.STOPPED
import com.twilio.audioswitch.simulateNewBluetoothHeadsetConnection
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class AudioDeviceSelectorTest : BaseTest() {

    internal val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }

    @Before
    fun setUp() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun `start should start the bluetooth and wired headset listeners`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        assertBluetoothHeadsetSetup()

        assertThat(wiredHeadsetReceiver.deviceListener, equalTo(audioDeviceSelector.wiredDeviceConnectionListener))
        verify(context).registerReceiver(eq(wiredHeadsetReceiver), isA())
    }

    @Test
    fun `start should transition to the started state if the current state is stopped`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        assertThat(audioDeviceSelector.state, equalTo(STARTED))
    }

    @Test
    fun `start should cache the default audio devices and the default selected audio device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        audioDeviceSelector.availableAudioDevices.let { audioDevices ->
            assertThat(audioDevices.size, equalTo(2))
            assertThat(audioDevices[0] is Earpiece, equalTo(true))
            assertThat(audioDevices[1] is Speakerphone, equalTo(true))
        }
        assertThat(audioDeviceSelector.selectedAudioDevice is Earpiece, equalTo(true))
    }

    @Test
    fun `start should invoke the audio device change listener with the default audio devices`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        verify(audioDeviceChangeListener).invoke(
                listOf(Earpiece(), Speakerphone()),
                Earpiece())
    }

    @Test
    fun `start should not start the HeadsetManager if it is null`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null
        )

        audioDeviceSelector.start(audioDeviceChangeListener)

        verify(bluetoothAdapter, times(0)).getProfileProxy(
                context,
                headsetManager,
                BluetoothProfile.HEADSET
        )
        verify(context, times(0)).registerReceiver(eq(headsetManager), isA())
    }

    @Test
    fun `start should do nothing if the current state is started`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        try {
            audioDeviceSelector.start(audioDeviceChangeListener)
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `start should do nothing if the current state is activated`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.start(audioDeviceChangeListener)

        try {
            audioDeviceSelector.start(audioDeviceChangeListener)
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `stop should transition to the stopped state if the current state is started`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        assertThat(audioDeviceSelector.state, equalTo(STOPPED))
    }

    @Test
    fun `stop should stop the bluetooth and wired headset listeners if the current state is started`() {
        headsetManager.onServiceConnected(0, headsetProxy)

        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        // Verify bluetooth behavior
        assertBluetoothHeadsetTeardown()

        // Verify wired headset behavior
        assertThat(wiredHeadsetReceiver.deviceListener, `is`(nullValue()))
        verify(context).unregisterReceiver(wiredHeadsetReceiver)
    }

    @Test
    fun `stop should transition to the stopped state if the current state is activated`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        headsetManager.onServiceConnected(0, bluetoothProfile)

        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        assertThat(audioDeviceSelector.state, equalTo(STOPPED))
    }

    @Ignore("Finish as part of https://issues.corp.twilio.com/browse/AHOYAPPS-588")
    @Test
    fun `stop should stop the bluetooth and wired headset listeners if the current state is activated`() {
        TODO("Implement after deactivate tests are complete")
    }

    @Test
    fun `stop should do nothing if the current state is stopped`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        try {
            audioDeviceSelector.stop()
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `stop should unassign the audio device change listener`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        assertThat(audioDeviceSelector.audioDeviceChangeListener, `is`(nullValue()))
    }

    @Test
    fun `stop should not stop the BluetoothHeadsetManager if it is null and if transitioning from the started state`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null
        )
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(headsetManager)
    }

    @Test
    fun `stop should not stop the BluetoothHeadsetManager if it is null and if transitioning from the activated state`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null
        )
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(headsetManager)
    }

    @Test
    fun `activate should transition to the activated state if the current state is started`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        assertThat(audioDeviceSelector.state, equalTo(ACTIVATED))
    }

    @Test
    fun `activate should set audio focus using Android O method if api version is 26`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(this.audioFocusRequest.buildRequest()).thenReturn(audioFocusRequest)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        verify(audioManager).requestAudioFocus(audioFocusRequest)
    }

    @Test
    fun `activate should set audio focus using Android O method if api version is 27`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O_MR1)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(this.audioFocusRequest.buildRequest()).thenReturn(audioFocusRequest)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        verify(audioManager).requestAudioFocus(audioFocusRequest)
    }

    @Test
    fun `activate should set audio focus using pre Android O method if api version is 25`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.N_MR1)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        verify(audioManager).requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 26`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(this.audioFocusRequest.buildRequest()).thenReturn(audioFocusRequest)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        verify(audioManager).abandonAudioFocusRequest(audioFocusRequest)
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 27`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.O_MR1)
        val audioFocusRequest = mock<AudioFocusRequest>()
        whenever(this.audioFocusRequest.buildRequest()).thenReturn(audioFocusRequest)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        verify(audioManager).abandonAudioFocusRequest(audioFocusRequest)
    }

    @Test
    fun `deactivate should abandon audio focus using pre Android O method if api version is 25`() {
        whenever(buildWrapper.getVersion()).thenReturn(Build.VERSION_CODES.N_MR1)
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        verify(audioManager).abandonAudioFocus(audioFocusChangeListener)
    }

    @Test
    fun `activate should enable audio routing to the earpiece`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        val earpiece = audioDeviceSelector.availableAudioDevices.find { it.name == "Earpiece" }
        audioDeviceSelector.selectDevice(earpiece)
        audioDeviceSelector.activate()

        verify(audioManager).isSpeakerphoneOn = false
    }

    @Test
    fun `activate should enable audio routing to the speakerphone device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.selectDevice(Speakerphone())
        audioDeviceSelector.activate()

        verify(audioManager).isSpeakerphoneOn = true
    }

    @Test
    fun `activate should enable audio routing to the bluetooth device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection()
        audioDeviceSelector.activate()

        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should enable audio routing to the wired headset device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.wiredDeviceConnectionListener.onDeviceConnected()
        audioDeviceSelector.activate()

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
    fun `onBluetoothDeviceStateChanged should enumerate devices`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection()
        audioDeviceSelector.activate()

        audioDeviceSelector.bluetoothDeviceConnectionListener.onBluetoothHeadsetStateChanged()

        verify(audioManager, times(2)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `selectDevice should not re activate the bluetooth device if the same device has been selected`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection()
        audioDeviceSelector.activate()

        audioDeviceSelector.selectDevice(AudioDevice.BluetoothHeadset(DEVICE_NAME))

        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }
}
