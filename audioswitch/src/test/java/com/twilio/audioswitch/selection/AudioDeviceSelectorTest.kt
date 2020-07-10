package com.twilio.audioswitch.selection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context
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
import com.twilio.audioswitch.android.BluetoothDeviceWrapper
import com.twilio.audioswitch.android.BluetoothIntentProcessorImpl
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothControllerAssertions
import com.twilio.audioswitch.bluetooth.BluetoothDeviceCacheManager
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.bluetooth.DisableBluetoothScoJob
import com.twilio.audioswitch.bluetooth.EnableBluetoothScoJob
import com.twilio.audioswitch.selection.AudioDevice.Earpiece
import com.twilio.audioswitch.selection.AudioDevice.Speakerphone
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.ACTIVATED
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.STARTED
import com.twilio.audioswitch.selection.AudioDeviceSelector.State.STOPPED
import com.twilio.audioswitch.setupAudioManagerMock
import com.twilio.audioswitch.setupScoHandlerMock
import com.twilio.audioswitch.setupSystemClockMock
import com.twilio.audioswitch.wired.WiredHeadsetReceiver
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test

class AudioDeviceSelectorTest {

    val bluetoothDevice = mock<BluetoothDeviceWrapper> {
        whenever(mock.name).thenReturn("Bluetooth")
    }
    private val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }
    private val context = mock<Context> {
        whenever(mock.packageManager).thenReturn(packageManager)
    }
    private val logger = mock<LogWrapper>()
    private val audioManager = setupAudioManagerMock()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
    private val deviceCache = BluetoothDeviceCacheManager(logger)
    private val bluetoothHeadsetManager = BluetoothHeadsetManager(logger, bluetoothAdapter,
            deviceCache)
    private val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    private val buildWrapper = mock<BuildWrapper>()
    private val audioFocusRequest = mock<AudioFocusRequestWrapper>()
    private val audioDeviceManager = AudioDeviceManager(context,
            logger,
            audioManager,
            buildWrapper,
            audioFocusRequest)
    private var handler = setupScoHandlerMock()
    private var systemClockWrapper = setupSystemClockMock()
    private var bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context,
            logger,
            BluetoothIntentProcessorImpl(),
            audioDeviceManager,
            deviceCache,
            EnableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper),
            DisableBluetoothScoJob(logger, audioDeviceManager, handler, systemClockWrapper))
    private var audioDeviceSelector = AudioDeviceSelector(
            logger,
            audioDeviceManager,
            wiredHeadsetReceiver,
            BluetoothController(
                    context,
                    bluetoothAdapter,
                    bluetoothHeadsetManager,
                    bluetoothHeadsetReceiver),
            deviceCache
    )
    private val bluetoothControllerAssertions = BluetoothControllerAssertions()

    @Test
    fun `start should start the bluetooth and wired headset listeners`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        bluetoothControllerAssertions.assertStart(
                context,
                bluetoothHeadsetManager,
                bluetoothHeadsetReceiver,
                audioDeviceSelector.bluetoothDeviceConnectionListener,
                bluetoothAdapter)

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
    fun `start should not start the BluetoothController if it is null`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null,
                deviceCache
        )

        audioDeviceSelector.start(audioDeviceChangeListener)

        bluetoothControllerAssertions.assertNotStarted(
                context,
                bluetoothHeadsetManager,
                bluetoothHeadsetReceiver,
                bluetoothAdapter)
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
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        // Verify bluetooth behavior
        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
        verify(context).unregisterReceiver(bluetoothHeadsetReceiver)

        // Verify wired headset behavior
        assertThat(wiredHeadsetReceiver.deviceListener, `is`(nullValue()))
        verify(context).unregisterReceiver(wiredHeadsetReceiver)
    }

    @Test
    fun `stop should transition to the stopped state if the current state is activated`() {
        val bluetoothProfile = mock<BluetoothHeadset>()
        bluetoothHeadsetManager.onServiceConnected(0, bluetoothProfile)

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
    fun `stop should not stop the BluetoothController if it is null and if transitioning from the started state`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null,
                deviceCache
        )
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(bluetoothHeadsetReceiver)
    }

    @Test
    fun `stop should not stop the BluetoothController if it is null and if transitioning from the activated state`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                null,
                deviceCache
        )
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()
        audioDeviceSelector.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(bluetoothHeadsetReceiver)
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
                isA(),
                eq(AudioManager.STREAM_VOICE_CALL),
                eq(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
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

        verify(audioManager).abandonAudioFocus(isA())
    }

    @Test
    fun `activate should enable audio routing to the earpiece`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).stopBluetoothSco()
    }

    @Test
    fun `activate should enable audio routing to the speakerphone device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.selectDevice(Speakerphone())
        audioDeviceSelector.activate()

        verify(audioManager).isSpeakerphoneOn = true
        verify(audioManager).stopBluetoothSco()
    }

    @Test
    fun `activate should enable audio routing to the bluetooth device`() {
        deviceCache.addDevice(AudioDevice.BluetoothHeadset(bluetoothDevice))
        audioDeviceSelector.start(audioDeviceChangeListener)
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
        verify(audioManager).stopBluetoothSco()
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
        audioDeviceSelector.activate()

        deviceCache.addDevice(AudioDevice.BluetoothHeadset(bluetoothDevice))
        audioDeviceSelector.bluetoothDeviceConnectionListener.onBluetoothDeviceStateChanged()

        verify(audioManager, times(2)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `selectDevice should not re activate the bluetooth device if the same device has been selected`() {
        deviceCache.addDevice(AudioDevice.BluetoothHeadset(bluetoothDevice))
        audioDeviceSelector.start(audioDeviceChangeListener)
        audioDeviceSelector.activate()

        audioDeviceSelector.selectDevice(AudioDevice.BluetoothHeadset(bluetoothDevice))

        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }
}
