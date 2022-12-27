package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.Intent
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
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivated
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivating
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.AudioActivationError
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Connected
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager.HeadsetState.Disconnected
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class BluetoothHeadsetManagerTest : BaseTest() {

    private val bluetoothDevices = listOf(expectedBluetoothDevice)

    @Test
    fun `onServiceConnected should notify the deviceListener if there are connected devices`() {
        val headsetManager = headsetManager
        setupConnectedState(headsetManager)

        verify(headsetManager.headsetListener)?.onBluetoothHeadsetStateChanged(DEVICE_NAME)
    }

    @Test
    fun `onServiceConnected should set the headset state to Connected if there are connected devices`() {
        val headsetManager = headsetManager
        setupConnectedState(headsetManager)

        assertThat(headsetManager.headsetState is Connected, equalTo(true))
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        val headsetManager = headsetManager
        val previousListener = headsetManager.headsetListener!!
        headsetManager.headsetListener = null
        setupConnectedState(headsetManager)

        verifyZeroInteractions(previousListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth headsets`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        val headsetManager = headsetManager
        headsetManager.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(headsetManager.headsetListener!!)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        val headsetManager = headsetManager
        headsetManager.onServiceDisconnected(0)

        verify(headsetManager.headsetListener!!).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onServiceDisconnected should set the headset state to Disconnected`() {
        val headsetManager = headsetManager
        setupConnectedState(headsetManager)
        headsetManager.onServiceDisconnected(0)

        assertThat(headsetManager.headsetState is Disconnected, equalTo(true))
    }

    @Test
    fun `onServiceDisconnected should not notify the deviceListener if deviceListener is null`() {
        val headsetManager = headsetManager
        val previousListener = headsetManager.headsetListener!!
        headsetManager.headsetListener = null
        headsetManager.onServiceDisconnected(0)

        verifyZeroInteractions(previousListener)
    }

    @Test
    fun `stop should close close all resources`() {
        val deviceListener = mock<BluetoothHeadsetConnectionListener>()
        val headsetManager = headsetManager
        headsetManager.headsetListener = deviceListener
        headsetManager.start(deviceListener)
        headsetManager.stop()

        assertBluetoothHeadsetTeardown(headsetManager)
    }

    @Test
    fun `start should successfully setup headset manager`() {
        val deviceListener = mock<BluetoothHeadsetConnectionListener>()
        val headsetManager = headsetManager
        headsetManager.start(deviceListener)

        assertBluetoothHeadsetSetup(headsetManager)
    }

    @Test
    fun `activate should start bluetooth device audio routing if state is Connected`() {
        val headsetManager = headsetManager
        headsetManager.headsetState = Connected
        headsetManager.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should start bluetooth device audio routing if state is AudioActivationError`() {
        val headsetManager = headsetManager
        headsetManager.headsetState = AudioActivationError

        headsetManager.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `activate should not start bluetooth device audio routing if state is Disconnected`() {
        val headsetManager = headsetManager
        headsetManager.headsetState = Disconnected

        headsetManager.activate()

        verifyZeroInteractions(audioManager)
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        val headsetManager = headsetManager
        headsetManager.headsetState = AudioActivated

        headsetManager.deactivate()

        verify(audioManager).stopBluetoothSco()
    }

    @Test
    fun `deactivate should not stop bluetooth device audio routing if state is AudioActivating`() {
        val headsetManager = headsetManager
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
    fun `onReceive should register a new device when a headset connection event is received`(
        deviceClass: BluetoothClass?,
        isNewDeviceConnected: Boolean
    ) {
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            whenever(mock.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED))
                .thenReturn(BluetoothHeadset.STATE_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
        }
        val headsetManager = headsetManager
        headsetManager.onReceive(context, intent)

        val invocationCount = if (isNewDeviceConnected) 1 else 0
        verify(headsetManager.headsetListener!!, times(invocationCount)).onBluetoothHeadsetStateChanged(DEVICE_NAME)
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should disconnect a device when a headset disconnection event is received`(
        deviceClass: BluetoothClass?,
        isDeviceDisconnected: Boolean
    ) {
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            whenever(mock.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED))
                .thenReturn(BluetoothHeadset.STATE_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
        }
        val headsetManager = headsetManager
        headsetManager.onReceive(context, intent)

        val invocationCount = if (isDeviceDisconnected) 1 else 0
        verify(headsetManager.headsetListener!!, times(invocationCount)).onBluetoothHeadsetStateChanged()
    }

    @Test
    fun `onReceive should not register a new device when an ACL connected event is received with a null bluetooth device`() {
        val headsetManager = headsetManager
        whenever(expectedBluetoothDevice.bluetoothClass).thenReturn(null)
        simulateNewBluetoothHeadsetConnection(headsetManager)

        verifyZeroInteractions(headsetManager.headsetListener!!)
    }

    @Test
    fun `onReceive should not register a new device when the deviceListener is null`() {
        val headsetManager = headsetManager
        val previous = headsetManager.headsetListener!!
        headsetManager.headsetListener = null
        simulateNewBluetoothHeadsetConnection(headsetManager)

        verifyZeroInteractions(previous)
    }

    @Test
    fun `onReceive should not disconnect a device when an ACL disconnected event is received with a null bluetooth device`() {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(null)
        }

        val headsetManager = headsetManager
        headsetManager.onReceive(mock(), intent)

        verifyZeroInteractions(headsetManager.headsetListener!!)
    }

    @Test
    fun `onReceive should not disconnect a device when the deviceListener is null`() {
        val headsetManager = headsetManager
        val previous = headsetManager.headsetListener!!
        headsetManager.headsetListener = null
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
        }

        headsetManager.onReceive(mock(), intent)

        verifyZeroInteractions(previous)
    }

    @Test
    fun `onReceive should receive no headset listener callbacks if the intent action is null`() {
        val headsetManager = headsetManager
        headsetManager.onReceive(mock(), mock())

        verifyZeroInteractions(headsetManager.headsetListener!!)
    }

    @Test
    fun `a headset audio connection should cancel a running enableBluetoothScoJob`() {
        val headsetManager = headsetManager
        setupConnectedState(headsetManager)
        headsetManager.activate()
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            whenever(mock.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED))
                .thenReturn(BluetoothHeadset.STATE_AUDIO_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
        }
        headsetManager.onReceive(context, intent)

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
    }

    @Test
    fun `a bluetooth headset audio disconnection should cancel a running disableBluetoothScoJob`() {
        val headsetManager = headsetManager
        headsetManager.headsetState = AudioActivated
        headsetManager.deactivate()
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            whenever(
                mock.getIntExtra(
                    BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_DISCONNECTED
                )
            )
                .thenReturn(BluetoothHeadset.STATE_AUDIO_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
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
        val headsetManager = headsetManager
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
        val headsetManager = headsetManager

        headsetManager.headsetState = Connected
        headsetManager.activate()

        verify(headsetManager.headsetListener!!).onBluetoothHeadsetActivationError()
    }

    @Test
    fun `EnableBluetoothScoJob scoTimeOutAction should not invoke the headset listener if it is null`() {
        systemClockWrapper = mock {
            whenever(mock.elapsedRealtime()).thenReturn(0L, TIMEOUT)
        }
        handler = setupHandlerMock()
        val headsetManager = BluetoothHeadsetManager(
            context,
            logger,
            bluetoothAdapter,
            audioDeviceManager,
            bluetoothScoHandler = handler,
            systemClockWrapper = systemClockWrapper,
            headsetProxy = headsetProxy,
            permissionsRequestStrategy = permissionsStrategyProxy,
        )

        headsetManager.headsetState = Connected
        headsetManager.activate()
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
        val headsetManager = headsetManager

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
        val headsetManager = headsetManager

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
        val headsetManager = headsetManager

        headsetManager.headsetState = Connected
        headsetManager.activate()

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
    }

    @Test
    fun `cancelBluetoothScoJob should not cancel sco runnable if it has not been initialized`() {
        headsetManager.enableBluetoothScoJob.cancelBluetoothScoJob()

        verifyZeroInteractions(handler)
    }

    @Test
    fun `it should cancel the enable bluetooth sco job when setting the state to disconnected`() {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices, bluetoothDevices, emptyList())
        }
        val headsetManager = headsetManager
        headsetManager.onServiceConnected(0, bluetoothProfile)
        headsetManager.activate()

        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            whenever(mock.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED))
                .thenReturn(BluetoothHeadset.STATE_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                .thenReturn(expectedBluetoothDevice)
        }
        headsetManager.onReceive(context, intent)

        assertScoJobIsCanceled(handler, headsetManager.enableBluetoothScoJob)
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

    private fun setupConnectedState(headsetManager: BluetoothHeadsetManager?) {
        val bluetoothProfile = mock<BluetoothHeadset> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }
        headsetManager?.onServiceConnected(0, bluetoothProfile)
    }

    private fun assertScoJobIsCanceled(handler: Handler, scoJob: BluetoothScoJob) {
        verify(handler).removeCallbacks(isA())
        assertThat(scoJob.bluetoothScoRunnable, `is`(nullValue()))
    }
}
