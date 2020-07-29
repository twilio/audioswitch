package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.BaseTest
import com.twilio.audioswitch.DEVICE_NAME
import com.twilio.audioswitch.assertBluetoothHeadsetSetup
import com.twilio.audioswitch.assertBluetoothHeadsetTeardown
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivated
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivating
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivationError
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Connected
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Disconnected
import com.twilio.audioswitch.simulateNewBluetoothHeadsetConnection
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
class BluetoothHeadsetManagerTest : BaseTest() {

    private val headsetListener = mock<BluetoothHeadsetConnectionListener>()
    private val bluetoothDevices = listOf(expectedBluetoothDevice)

    @Before
    fun setUp() {
        initializeManagerWithMocks()
    }

    @Test
    fun `onServiceConnected should notify the deviceListener if there are connected devices`() {
        setupConnectedState()

        verify(headsetListener).onBluetoothHeadsetStateChanged(DEVICE_NAME)
    }

    @Test
    fun `onServiceConnected should set the headset state to Connected if there are connected devices`() {
        setupConnectedState()

        assertThat(headsetManager.headsetState is Connected, equalTo(true))
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        headsetManager.headsetListener = null
        setupConnectedState()

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth headsets`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        headsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        headsetManager.onServiceDisconnected(0)

        verify(headsetListener).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceDisconnected should set the headset state to Disconnected`() {
        setupConnectedState()
        headsetManager.onServiceDisconnected(0)

        assertThat(headsetManager.headsetState is Disconnected, equalTo(true))
    }

    @Test
    fun `onServiceDisconnected should not notify the deviceListener if deviceListener is null`() {
        headsetManager.headsetListener = null
        headsetManager.onServiceDisconnected(0)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `stop should close close all resources`() {
        headsetManager.stop()

        assertBluetoothHeadsetTeardown()
    }

    @Test
    fun `start should successfully setup headset manager`() {
        val deviceListener = mock<BluetoothHeadsetConnectionListener>()
        headsetManager.start(deviceListener)

        assertBluetoothHeadsetSetup()
    }

    @Test
    fun `activate should start bluetooth device audio routing if state is Connected`() {
        headsetManager.headsetState = Connected
        headsetManager.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should start bluetooth device audio routing if state is AudioActivationError`() {
        headsetManager.headsetState = AudioActivationError

        headsetManager.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should not start bluetooth device audio routing if state is Disconnected`() {
        headsetManager.headsetState = Disconnected

        headsetManager.activate()

        verifyZeroInteractions(audioManager)
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        headsetManager.headsetState = AudioActivated

        headsetManager.deactivate()

        verify(audioManager).stopBluetoothSco()
    }

    @Test
    fun `deactivate should not stop bluetooth device audio routing if state is AudioActivating`() {
        headsetManager.headsetState = AudioActivating

        headsetManager.deactivate()

        verifyZeroInteractions(audioManager)
    }

    fun parameters(): Array<Array<out Any?>> {
        val handsFreeDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE)
        }
        val audioVideoHeadsetDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)
        }
        val audioVideoCarDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO)
        }
        val headphonesDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
        }
        val uncategorizedDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.Major.UNCATEGORIZED)
        }
        val wrongDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR)
        }
        return arrayOf(
                arrayOf(handsFreeDevice, true),
                arrayOf(audioVideoHeadsetDevice, true),
                arrayOf(audioVideoCarDevice, true),
                arrayOf(headphonesDevice, true),
                arrayOf(uncategorizedDevice, true),
                arrayOf(wrongDevice, false),
                arrayOf(null, false)
        )
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should register a new device when an ACL connected event is received`(
        deviceClass: BluetoothClass?,
        isNewDeviceConnected: Boolean
    ) {
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        simulateNewBluetoothHeadsetConnection(expectedBluetoothDevice)

        val invocationCount = if (isNewDeviceConnected) 1 else 0
        verify(headsetListener, times(invocationCount)).onBluetoothHeadsetStateChanged(DEVICE_NAME)
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should disconnect a device when an ACL disconnected event is received`(
        deviceClass: BluetoothClass?,
        isDeviceDisconnected: Boolean
    ) {
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(expectedBluetoothDevice)
        }

        headsetManager.onReceive(context, intent)

        val invocationCount = if (isDeviceDisconnected) 1 else 0
        verify(headsetListener, times(invocationCount)).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onReceive should not register a new device when an ACL connected event is received with a null bluetooth device`() {
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(null)
        simulateNewBluetoothHeadsetConnection()

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onReceive should not register a new device when the deviceListener is null`() {
        headsetManager.headsetListener = null
        simulateNewBluetoothHeadsetConnection()

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onReceive should not disconnect a device when an ACL disconnected event is received with a null bluetooth device`() {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(null)
        }

        headsetManager.onReceive(mock(), intent)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onReceive should not disconnect a device when the deviceListener is null`() {
        headsetManager.headsetListener = null
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(expectedBluetoothDevice)
        }

        headsetManager.onReceive(mock(), intent)

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `onReceive should receive no headset listener callbacks if the intent action is null`() {
        headsetManager.onReceive(mock(), mock())

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `SCO_AUDIO_STATE_CONNECTED should cancel a running enableBluetoothScoJob`() {
        headsetManager.headsetState = Connected
        headsetManager.activate()
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            whenever(mock.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR))
                    .thenReturn(AudioManager.SCO_AUDIO_STATE_CONNECTED)
        }
        headsetManager.onReceive(mock(), intent)

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
    }

    @Test
    fun `SCO_AUDIO_STATE_DISCONNECTED should cancel a running disableBluetoothScoJob`() {
        headsetManager.headsetState = AudioActivated
        headsetManager.deactivate()
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            whenever(mock.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR))
                    .thenReturn(AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
        }
        headsetManager.onReceive(mock(), intent)

        assertScoJobIsCanceled(handler, headsetManager.disableBluetoothScoJob)
    }

    @Test
    fun `EnableBluetoothScoJob scoTimeOutAction should set the state to AudioActivationError`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        initializeManagerWithMocks()
        headsetManager.headsetState = Connected
        headsetManager.activate()

        assertThat(headsetManager.headsetState is AudioActivationError, equalTo(true))
    }

    @Test
    fun `EnableBluetoothScoJob scoTimeOutAction should invoke the headset listener`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        initializeManagerWithMocks()
        headsetManager.headsetState = Connected
        headsetManager.activate()

        verify(headsetListener).onBluetoothHeadsetActivationError()
    }

    @Test
    fun `EnableBluetoothScoJob scoTimeOutAction should not invoke the headset listener if it is null`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        headsetManager = BluetoothHeadsetManager(context, logger, bluetoothAdapter,
                audioDeviceManager, bluetoothScoHandler = handler,
                systemClockWrapper = systemClockWrapper, headsetProxy = headsetProxy)
        headsetManager.headsetState = Connected
        headsetManager.activate()

        verifyZeroInteractions(headsetListener)
    }

    @Test
    fun `BluetoothScoRunnable should execute enableBluetoothSco multiple times if not canceled`() {
        handler = mock {
            whenever(mock.post(any())).thenAnswer {
                (it.arguments[0] as BluetoothScoJob.BluetoothScoRunnable).run()
                true
            }

            var firstInvocation = true
            whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                if (firstInvocation) {
                    firstInvocation = false
                    (it.arguments[0] as BluetoothScoJob.BluetoothScoRunnable).run()
                }
                true
            }
        }
        initializeManagerWithMocks()
        headsetManager.headsetState = Connected
        headsetManager.activate()

        verify(audioManager, times(2)).startBluetoothSco()
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime equals the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        initializeManagerWithMocks()
        headsetManager.headsetState = Connected
        headsetManager.activate()

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
    }

    @Test
    fun `BluetoothScoRunnable should timeout if elapsedTime is greater than the time limit`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT + 1000)
        }
        handler = setupHandlerMock()
        initializeManagerWithMocks()
        headsetManager.headsetState = Connected
        headsetManager.activate()

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
    }

    @Test
    fun `cancelBluetoothScoJob should not cancel sco runnable if it has not been initialized`() {
        headsetManager.enableBluetoothScoJob.cancelBluetoothScoJob()

        verifyZeroInteractions(handler)
    }

    private fun setupHandlerMock() =
            mock<Handler> {
                whenever(mock.post(any())).thenAnswer {
                    (it.arguments[0] as BluetoothScoJob.BluetoothScoRunnable).run()
                    true
                }

                whenever(mock.postDelayed(isA(), isA())).thenAnswer {
                    (it.arguments[0] as BluetoothScoJob.BluetoothScoRunnable).run()
                    true
                }
            }

    private fun setupConnectedState() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }
        headsetManager.onServiceConnected(0, bluetoothProfile)
    }

    private fun assertScoJobIsCanceled(handler: Handler, scoJob: BluetoothScoJob) {
        verify(handler).removeCallbacks(isA())
        assertThat(scoJob.bluetoothScoRunnable, `is`(nullValue()))
    }

    private fun initializeManagerWithMocks() {
        headsetManager = BluetoothHeadsetManager(context, logger, bluetoothAdapter,
                audioDeviceManager, headsetListener, handler, systemClockWrapper,
                headsetProxy = headsetProxy)
    }
}
