package com.twilio.audioswitch

import android.content.Intent
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.scanners.AudioDeviceScanner
import com.twilio.audioswitch.wired.INTENT_STATE
import com.twilio.audioswitch.wired.STATE_PLUGGED
import com.twilio.audioswitch.wired.STATE_UNPLUGGED
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class ModernAudioSwitchTest : BaseTest() {

    internal val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }

    @Before
    fun setUp() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    @Test
    fun `start should start listeners to receive devices`() {
        val audioSwitch = getModernAudioSwitch()
        val scanner = audioSwitch.deviceScanner as AudioDeviceScanner
        audioSwitch.start(audioDeviceChangeListener)

        assertThat(scanner.listener, equalTo(audioSwitch))
        verify(audioManager).registerAudioDeviceCallback(scanner, handler)
    }

    @Test
    fun `stop should stop listeners if the current state is started`() {
        val audioSwitch = getModernAudioSwitch()
        val scanner = audioSwitch.deviceScanner as AudioDeviceScanner

        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.stop()

        assertThat(audioSwitch.audioDeviceChangeListener, `is`(nullValue()))
        verify(audioManager).unregisterAudioDeviceCallback(scanner)
    }

    @Test
    fun `stop should transition to the stopped state if the current state is activated`() {
        val audioSwitch = getModernAudioSwitch()

        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.activate()
        audioSwitch.stop()

        assertThat(audioSwitch.state, equalTo(AbstractAudioSwitch.State.STOPPED))
    }

    @Test
    fun `activate should enable audio routing to the bluetooth device`() {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
        audioSwitch.activate()

        verify(audioManager).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should enable audio routing to the wired headset device`() {
        val audioSwitch = getModernAudioSwitch(
            setupAudioDeviceScannerMock(),
        )
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.WiredHeadset())
        audioSwitch.activate()

        verify(audioManager).isSpeakerphoneOn = false
    }

    @Test
    fun `onBluetoothDeviceStateChanged should enumerate devices`() {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset(expectedBluetoothDevice.name))
        audioSwitch.activate()

        verify(audioManager, atLeast(1)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `selectDevice should not re activate the bluetooth device if the same device has been selected`() {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset(expectedBluetoothDevice.name))
        audioSwitch.activate()

        audioSwitch.selectDevice(AudioDevice.BluetoothHeadset(expectedBluetoothDevice.name))

        verify(audioManager, atLeast(1)).isSpeakerphoneOn = false
        verify(audioManager).startBluetoothSco()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor should throw an IllegalArgumentException given duplicate preferred devices`() {
        AudioSwitch(
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
            handler = handler,
        )
    }

    @Test
    fun `a new bluetooth headset should automatically connect if it is the user's selection`() {
        // Switch selection order so BT isn't automatically selected
        val secondBluetoothDevice = AudioDevice.BluetoothHeadset("$DEVICE_NAME 2")
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock(secondBluetoothDevice))

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)

            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset("$DEVICE_NAME 2"))

            val bluetoothDevice = availableAudioDevices.find { it is AudioDevice.BluetoothHeadset }
            selectDevice(bluetoothDevice)
            activate()

            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset("$DEVICE_NAME 2"))

            assertThat(selectedAudioDevice, equalTo(AudioDevice.BluetoothHeadset(secondBluetoothDevice.name)))
        }
    }

    @Parameters(source = EarpieceAndSpeakerParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)
            activate()
            audioSwitch.onDeviceConnected(expectedDevice)

            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = WiredHeadsetParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated with a wired headset connected`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)
            activate()

            audioSwitch.onDeviceConnected(AudioDevice.WiredHeadset())
            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = BluetoothHeadsetParams::class)
    @Test
    fun `when configuring a new preferred device list, the correct device should be automatically selected and activated with a bluetooth headset connected`(
        preferredDeviceList: List<Class<out AudioDevice>>,
        expectedDevice: AudioDevice
    ) {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)
            activate()

            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())

            assertThat(selectedAudioDevice, equalTo(expectedDevice))
        }
    }

    @Parameters(source = DefaultDeviceParams::class)
    @Test
    fun `when configuring a new preferred device list, all connected devices should be available but earpiece when a wired headset is connected`(
        preferredDeviceList: List<Class<out AudioDevice>>
    ) {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)
            activate()

            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.WiredHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

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
    fun `when configuring a new preferred device list, all connected devices should be available but the wired headset`(
        preferredDeviceList: List<Class<out AudioDevice>>
    ) {
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())

        audioSwitch.run {
            start(this@ModernAudioSwitchTest.audioDeviceChangeListener)
            activate()

            audioSwitch.onDeviceConnected(AudioDevice.BluetoothHeadset())
            audioSwitch.onDeviceConnected(AudioDevice.Earpiece())
            audioSwitch.onDeviceConnected(AudioDevice.Speakerphone())

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
        val audioSwitch = getModernAudioSwitch(setupAudioDeviceScannerMock())
        audioSwitch.start(audioDeviceChangeListener)
        audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

        audioSwitch.activate()
        audioSwitch.onDeviceConnected(AudioDevice.Earpiece())

        verify(audioSwitch.audioDeviceChangeListener, times(1))?.invoke(
            listOf(AudioDevice.Earpiece()),
            AudioDevice.Earpiece()
        )

    }

    private fun simulateNewWiredHeadsetConnection() {
        val intent = mock<Intent> {
            whenever(mock.getIntExtra(INTENT_STATE, STATE_UNPLUGGED))
                .thenReturn(STATE_PLUGGED)
        }
        wiredHeadsetReceiver.onReceive(context, intent)
    }
}
