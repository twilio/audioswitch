package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.scanners.LegacyAudioDeviceScanner
import com.twilio.audioswitch.wired.INTENT_STATE
import com.twilio.audioswitch.wired.STATE_PLUGGED
import com.twilio.audioswitch.wired.STATE_UNPLUGGED
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class LegacyAudioSwitchTest : BaseTest() {

    internal val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }

    @Before
    fun setUp() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun `start should start listeners to receive devices`() {
        val audioSwitch = legacyAudioSwitch
        val scanner = audioSwitch.deviceScanner as LegacyAudioDeviceScanner
        audioSwitch.start(audioDeviceChangeListener)

        assertBluetoothHeadsetSetup(audioSwitch.headsetManager)
        assertThat(wiredHeadsetReceiver.deviceListener, equalTo(scanner.wiredDeviceConnectionListener))
        verify(context).registerReceiver(eq(wiredHeadsetReceiver), isA())
    }

    @Test
    fun `start should invoke the audio device change listener with the default audio devices`() {
        val audioSwitch = legacyAudioSwitch
        audioSwitch.start(audioDeviceChangeListener)

        verify(audioDeviceChangeListener).invoke(
            listOf(AudioDevice.Earpiece()),
            AudioDevice.Earpiece()
        )

        verify(audioDeviceChangeListener).invoke(
            listOf(AudioDevice.Earpiece(), AudioDevice.Speakerphone()),
            AudioDevice.Earpiece()
        )
    }

    @Test
    fun `start should not start the HeadsetManager if it is null`() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return
        }
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            headsetManager = null,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            audioManager = audioManager,
        )

        audioSwitch.start(audioDeviceChangeListener)

        verify(bluetoothAdapter, times(0)).getProfileProxy(
            context,
            audioSwitch.headsetManager,
            BluetoothProfile.HEADSET
        )
        verify(context, times(0)).registerReceiver(eq(audioSwitch.headsetManager), isA())
    }

    @Test
    fun `stop should stop listeners if the current state is started`() {
        val audioSwitch = legacyAudioSwitch

        audioSwitch.headsetManager?.onServiceConnected(0, headsetProxy)

        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        // Verify bluetooth behavior
        assertBluetoothHeadsetTeardown(audioSwitch.headsetManager)

        // Verify wired headset behavior
        assertThat(wiredHeadsetReceiver.deviceListener, `is`(nullValue()))
        verify(context).unregisterReceiver(wiredHeadsetReceiver)
    }

    @Test
    fun `stop should transition to the stopped state if the current state is activated`() {
        val audioSwitch = legacyAudioSwitch

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            val bluetoothProfile = mock<BluetoothHeadset>()
            headsetManager.onServiceConnected(0, bluetoothProfile)
        }
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        assertThat(audioSwitch.state, equalTo(AbstractAudioSwitch.State.STOPPED))
    }

    @Test
    fun `stop should not stop the BluetoothHeadsetManager if it is null and if transitioning from the started state`() {
        val headsetManager = headsetManager
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            audioManager = audioManager,
            headsetManager = null,
            scanner = getLegacyDeviceScanner(null),
        )
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(headsetManager)
    }

    @Test
    fun `stop should not stop the BluetoothHeadsetManager if it is null and if transitioning from the activated state`() {
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            audioManager = audioManager,
            headsetManager = null,
            scanner = getLegacyDeviceScanner(null),
        )
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        verifyZeroInteractions(bluetoothAdapter)
        verify(context, times(0)).unregisterReceiver(audioSwitch.headsetManager)
    }

    @Test
    fun `activate should enable audio routing to the bluetooth device`() {
        val audioSwitch = legacyAudioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
        audioSwitch.activate()

//        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `onBluetoothDeviceStateChanged should enumerate devices`() {
        val audioSwitch = legacyAudioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
        audioSwitch.activate()

        verify(audioManager, atLeast(1)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `selectDevice should not re activate the bluetooth device if the same device has been selected`() {
        val audioSwitch = legacyAudioSwitch

        audioSwitch.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
        audioSwitch.activate()

        audioSwitch.selectDevice(AudioDevice.BluetoothHeadset(expectedBluetoothDevice.name))

        verify(audioManager, atLeast(1)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw an IllegalArgumentException given duplicate preferred devices`() {
        LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = listOf(
                AudioDevice.Speakerphone::class.java,
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.Earpiece::class.java,
                AudioDevice.Speakerphone::class.java
            ),
            audioManager = audioManager,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
        )
    }

    @Test
    fun `a new bluetooth headset should automatically connect if it is the user's selection`() {
        // Switch selection order so BT isn't automatically selected
        val audioSwitch = legacyAudioSwitch
        val secondBluetoothDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("$DEVICE_NAME 2")
            whenever(mock.bluetoothClass).thenReturn(bluetoothClass)
        }

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager, secondBluetoothDevice)

            val bluetoothDevice = availableAudioDevices.find { it is AudioDevice.BluetoothHeadset }
            selectDevice(bluetoothDevice)
            activate()
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            assertThat(selectedAudioDevice, equalTo(AudioDevice.BluetoothHeadset(secondBluetoothDevice.name)))
        }
    }

    @Parameters(source = WiredHeadsetParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated with a wired headset connected`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            audioManager = audioManager,
            scanner = scanner,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            activate()
            simulateNewWiredHeadsetConnection()
            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = BluetoothHeadsetParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated with a bluetooth headset connected`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val headsetManager = headsetManager
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            activate()
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when configuring a new preferred device list, all connected devices should be available but earpiece when a wired headset is connected`(
        preferredDeviceList: List<Class<out AudioDevice>>
    ) {
        val headsetManager = headsetManager
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            activate()
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            simulateNewWiredHeadsetConnection()

            assertThat(availableAudioDevices.size, equalTo(3))
            assertThat(
                availableAudioDevices.containsAll(
                    listOf(
                        AudioDevice.BluetoothHeadset(),
                        AudioDevice.WiredHeadset(),
                        AudioDevice.Speakerphone()
                    )
                ),
                equalTo(true)
            )
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when configuring a new preferred device list, all connected devices should be available but wired headset after plugging and unplugging wired headset`(
        preferredDeviceList: List<Class<out AudioDevice>>
    ) {
        val headsetManager = headsetManager
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            activate()
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            simulateNewWiredHeadsetConnection()
            simulateWiredHeadsetDisconnection()

            assertThat(availableAudioDevices.size, equalTo(3))
            assertThat(
                availableAudioDevices.containsAll(
                    listOf(
                        AudioDevice.BluetoothHeadset(),
                        AudioDevice.Speakerphone(),
                        AudioDevice.Earpiece()
                    )
                ),
                equalTo(true)
            )
        }
    }


    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when configuring a new preferred device list, all connected devices should be available but the wired headset`(
        preferredDeviceList: List<Class<out AudioDevice>>
    ) {
        val headsetManager = headsetManager
        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            headsetManager = headsetManager,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            activate()

            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)

            assertThat(availableAudioDevices.size, equalTo(3))
            assertThat(
                availableAudioDevices.containsAll(
                    listOf(
                        AudioDevice.BluetoothHeadset(),
                        AudioDevice.Earpiece(),
                        AudioDevice.Speakerphone()
                    )
                ),
                equalTo(true)
            )
        }
    }

    @Test
    fun `Upon receiving redundant system events, redundant onAudioChanged events shall not be triggered`() {
        val audioSwitch = legacyAudioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
        audioSwitch.activate()
        // additional disconnects should not invoke the listener, after the first disconnect,
        // device list and selected device should not change
        simulateDisconnectedBluetoothHeadsetConnection(audioSwitch.headsetManager)
        simulateDisconnectedBluetoothHeadsetConnection(audioSwitch.headsetManager)
        verify(audioDeviceChangeListener, times(2)).invoke(
            listOf(AudioDevice.Earpiece(), AudioDevice.Speakerphone()),
            AudioDevice.Earpiece()
        )
    }


    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when audio mode is not in communication, audio routing should not happen`(
        preferredDeviceList: List<Class<out AudioDevice>>,
    ) {
        val audioDeviceManagerSpy = spy(audioDeviceManager)
        val headsetManager = BluetoothHeadsetManager(
            context,
            logger,
            bluetoothAdapter,
            audioDeviceManagerSpy,
            bluetoothScoHandler = handler,
            systemClockWrapper = systemClockWrapper,
            headsetProxy = headsetProxy,
            permissionsRequestStrategy = permissionsStrategyProxy,
            headsetListener = mock()
        )

        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManagerSpy,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            headsetManager = headsetManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())
            audioMode = AudioManager.MODE_NORMAL
            activate()

            verify(audioDeviceManagerSpy, never()).enableBluetoothSco(any())
            verify(audioDeviceManagerSpy, never()).enableSpeakerphone(any())
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when audio mode is not in communication, audio routing happens when forced`(
        preferredDeviceList: List<Class<out AudioDevice>>,
    ) {
        val audioDeviceManagerSpy = spy(audioDeviceManager)
        val headsetManager = BluetoothHeadsetManager(
            context,
            logger,
            bluetoothAdapter,
            audioDeviceManagerSpy,
            bluetoothScoHandler = handler,
            systemClockWrapper = systemClockWrapper,
            headsetProxy = headsetProxy,
            permissionsRequestStrategy = permissionsStrategyProxy,
            headsetListener = mock()
        )

        val audioSwitch = LegacyAudioSwitch(
            context = context,
            logger = logger,
            audioDeviceManager = audioDeviceManagerSpy,
            wiredHeadsetReceiver = wiredHeadsetReceiver,
            headsetManager = headsetManager,
            audioFocusChangeListener = defaultAudioFocusChangeListener,
            preferredDeviceList = preferredDeviceList,
            scanner = getLegacyDeviceScanner(headsetManager),
            audioManager = audioManager,
        )

        audioSwitch.run {
            start(this@LegacyAudioSwitchTest.audioDeviceChangeListener)
            simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
            onDeviceConnected(AudioDevice.Speakerphone())
            onDeviceConnected(AudioDevice.Earpiece())
            audioMode = AudioManager.MODE_NORMAL
            forceHandleAudioRouting = true
            activate()

            verify(audioDeviceManagerSpy, atLeastOnce()).enableSpeakerphone(any())
        }
    }

    @Test
    fun `deactivate should disable bluetooth sco`() {
        val audioSwitch = legacyAudioSwitch
        audioSwitch.start(audioDeviceChangeListener)
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager)
        audioSwitch.activate()
        simulateNewBluetoothHeadsetConnection(audioSwitch.headsetManager, state = BluetoothHeadset.STATE_AUDIO_CONNECTED)
        audioSwitch.deactivate()

        verify(audioManager, times(1)).stopBluetoothSco()
    }

    private fun simulateNewWiredHeadsetConnection() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra(INTENT_STATE, STATE_UNPLUGGED))
                .thenReturn(STATE_PLUGGED)
        }
        wiredHeadsetReceiver.onReceive(context, intent)
    }

    private fun simulateWiredHeadsetDisconnection() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra(INTENT_STATE, STATE_UNPLUGGED))
                .thenReturn(STATE_UNPLUGGED)
        }
        wiredHeadsetReceiver.onReceive(context, intent)
    }
}
