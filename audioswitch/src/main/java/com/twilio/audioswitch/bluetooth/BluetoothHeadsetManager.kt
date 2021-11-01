package com.twilio.audioswitch.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED
import android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
import android.bluetooth.BluetoothHeadset.STATE_AUDIO_CONNECTED
import android.bluetooth.BluetoothHeadset.STATE_AUDIO_DISCONNECTED
import android.bluetooth.BluetoothHeadset.STATE_CONNECTED
import android.bluetooth.BluetoothHeadset.STATE_DISCONNECTED
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDeviceManager
import com.twilio.audioswitch.android.BluetoothDeviceWrapper
import com.twilio.audioswitch.android.BluetoothIntentProcessor
import com.twilio.audioswitch.android.BluetoothIntentProcessorImpl
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.android.SystemClockWrapper


private const val TAG = "BluetoothHeadsetManager"


internal interface BluetoothHeadsetManager {
    fun start(headsetListener: BluetoothHeadsetConnectionListener)
    fun stop()
    fun activate()
    fun deactivate()
    fun hasActivationError() : Boolean
    fun getHeadset(bluetoothHeadsetName: String?) : AudioDevice.BluetoothHeadset?

    companion object {
        fun newInstance(
            context: Context,
            logger: Logger,
            bluetoothAdapter: BluetoothAdapter?,
            audioDeviceManager: AudioDeviceManager
        ): BluetoothHeadsetManager? {
            return bluetoothAdapter?.let { adapter ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    BluetoothHeadsetManager_v31plus(context, logger, adapter, audioDeviceManager)
                } else {
                    BluetoothHeadsetManagerDefault(context, logger, adapter, audioDeviceManager)
                }
            } ?: run {
                logger.d(TAG, "Bluetooth is not supported on this device")
                null
            }
        }
    }
}

internal open class BluetoothHeadsetManagerDefault

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal constructor(
    internal val context: Context,
    internal val logger: Logger,
    private val bluetoothAdapter: BluetoothAdapter,
    audioDeviceManager: AudioDeviceManager,
    var headsetListener: BluetoothHeadsetConnectionListener? = null,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper(),
    private val bluetoothIntentProcessor: BluetoothIntentProcessor =
            BluetoothIntentProcessorImpl(),
    private var headsetProxy: BluetoothHeadset? = null
) : BluetoothHeadsetManager,
    BluetoothProfile.ServiceListener,
    BroadcastReceiver() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var headsetState: HeadsetState = HeadsetState.Disconnected
        set(value) {
            if (field != value) {
                field = value
                logger.d(TAG, "Headset state changed to ${field::class.simpleName}")
                if (value == HeadsetState.Disconnected) enableBluetoothScoJob.cancelBluetoothScoJob()
            }
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val enableBluetoothScoJob: EnableBluetoothScoJob = EnableBluetoothScoJob(logger,
            audioDeviceManager, bluetoothScoHandler, systemClockWrapper)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val disableBluetoothScoJob: DisableBluetoothScoJob = DisableBluetoothScoJob(logger,
            audioDeviceManager, bluetoothScoHandler, systemClockWrapper)

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.forEach { device ->
            logger.d(TAG, "Bluetooth " + device.name + " connected")
        }
        if (hasConnectedDevice()) {
            connect()
            headsetListener?.onBluetoothHeadsetStateChanged(getHeadsetName())
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        headsetState = HeadsetState.Disconnected
        headsetListener?.onBluetoothHeadsetStateChanged()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isCorrectIntentAction(intent.action)) {
            intent.getHeadsetDevice()?.let { bluetoothDevice ->
                intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, STATE_DISCONNECTED).let { state ->
                    when (state) {
                        STATE_CONNECTED -> {
                            logger.d(
                                    TAG,
                                    "Bluetooth headset $bluetoothDevice connected")
                            connect()
                            headsetListener?.onBluetoothHeadsetStateChanged(bluetoothDevice.name)
                        }
                        STATE_DISCONNECTED -> {
                            logger.d(
                                    TAG,
                                    "Bluetooth headset $bluetoothDevice disconnected")
                            disconnect()
                            headsetListener?.onBluetoothHeadsetStateChanged()
                        }
                        STATE_AUDIO_CONNECTED -> {
                            logger.d(TAG, "Bluetooth audio connected on device $bluetoothDevice")
                            enableBluetoothScoJob.cancelBluetoothScoJob()
                            headsetState = HeadsetState.AudioActivated
                            headsetListener?.onBluetoothHeadsetStateChanged()
                        }
                        STATE_AUDIO_DISCONNECTED -> {
                            logger.d(TAG, "Bluetooth audio disconnected on device $bluetoothDevice")
                            disableBluetoothScoJob.cancelBluetoothScoJob()
                            /*
                             * This block is needed to restart bluetooth SCO in the event that
                             * the active bluetooth headset has changed.
                             */
                            if (hasActiveHeadsetChanged()) {
                                enableBluetoothScoJob.executeBluetoothScoJob()
                            }

                            headsetListener?.onBluetoothHeadsetStateChanged()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun start(headsetListener: BluetoothHeadsetConnectionListener) {
        this.headsetListener = headsetListener

        bluetoothAdapter.getProfileProxy(
                context,
                this,
                BluetoothProfile.HEADSET)

        context.registerReceiver(
                this, IntentFilter(ACTION_CONNECTION_STATE_CHANGED))
        context.registerReceiver(
                this, IntentFilter(ACTION_AUDIO_STATE_CHANGED))
    }

    override fun stop() {
        headsetListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
        context.unregisterReceiver(this)
    }

    override fun activate() {
        if (headsetState == HeadsetState.Connected || headsetState == HeadsetState.AudioActivationError)
            enableBluetoothScoJob.executeBluetoothScoJob()
        else {
            logger.w(TAG, "Cannot activate when in the ${headsetState::class.simpleName} state")
        }
    }

    override fun deactivate() {
        if (headsetState == HeadsetState.AudioActivated) {
            disableBluetoothScoJob.executeBluetoothScoJob()
        } else {
            logger.w(TAG, "Cannot deactivate when in the ${headsetState::class.simpleName} state")
        }
    }

    override fun hasActivationError() = headsetState == HeadsetState.AudioActivationError

    // TODO Remove bluetoothHeadsetName param
    override fun getHeadset(bluetoothHeadsetName: String?) =
            if (headsetState != HeadsetState.Disconnected) {
                val headsetName = bluetoothHeadsetName ?: getHeadsetName()
                headsetName?.let { AudioDevice.BluetoothHeadset(it) }
                        ?: AudioDevice.BluetoothHeadset()
            } else null

    private fun isCorrectIntentAction(intentAction: String?) =
            intentAction == ACTION_CONNECTION_STATE_CHANGED || intentAction == ACTION_AUDIO_STATE_CHANGED

    private fun connect() {
        if (!hasActiveHeadset()) headsetState = HeadsetState.Connected
    }

    private fun disconnect() {
        headsetState = when {
            hasActiveHeadset() -> {
                HeadsetState.AudioActivated
            }
            hasConnectedDevice() -> {
                HeadsetState.Connected
            }
            else -> {
                HeadsetState.Disconnected
            }
        }
    }

    private fun hasActiveHeadsetChanged() = headsetState == HeadsetState.AudioActivated && hasConnectedDevice() && !hasActiveHeadset()

    private fun getHeadsetName(): String? =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    when {
                        devices.size > 1 && hasActiveHeadset() -> {
                            val device = devices.find { proxy.isAudioConnected(it) }?.name
                            logger.d(TAG, "Device size > 1 with device name: $device")
                            device
                        }
                        devices.size == 1 -> {
                            val device = devices.first().name
                            logger.d(TAG, "Device size 1 with device name: $device")
                            device
                        }
                        else -> {
                            logger.d(TAG, "Device size 0")
                            null
                        }
                    }
                }
            }

    private fun hasActiveHeadset() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.any { proxy.isAudioConnected(it) }
                }
            } ?: false

    private fun hasConnectedDevice() =
            headsetProxy?.let { proxy ->
                proxy.connectedDevices?.let { devices ->
                    devices.isNotEmpty()
                }
            } ?: false

    private fun Intent.getHeadsetDevice(): BluetoothDeviceWrapper? =
            bluetoothIntentProcessor.getBluetoothDevice(this)?.let { device ->
                if (isHeadsetDevice(device)) device else null
            }

    private fun isHeadsetDevice(deviceWrapper: BluetoothDeviceWrapper): Boolean =
            deviceWrapper.deviceClass?.let { deviceClass ->
                deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE ||
                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ||
                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ||
                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                        deviceClass == BluetoothClass.Device.Major.UNCATEGORIZED
            } ?: false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal sealed class HeadsetState {
        object Disconnected : HeadsetState()
        object Connected : HeadsetState()
        object AudioActivating : HeadsetState()
        object AudioActivationError : HeadsetState()
        object AudioActivated : HeadsetState()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal inner class EnableBluetoothScoJob(
        private val logger: Logger,
        private val audioDeviceManager: AudioDeviceManager,
        bluetoothScoHandler: Handler,
        systemClockWrapper: SystemClockWrapper
    ) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

        override fun scoAction() {
            logger.d(TAG, "Attempting to enable bluetooth SCO")
            audioDeviceManager.enableBluetoothSco(true)
            headsetState = HeadsetState.AudioActivating
        }

        override fun scoTimeOutAction() {
            headsetState = HeadsetState.AudioActivationError
            headsetListener?.onBluetoothHeadsetActivationError()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal inner class DisableBluetoothScoJob(
        private val logger: Logger,
        private val audioDeviceManager: AudioDeviceManager,
        bluetoothScoHandler: Handler,
        systemClockWrapper: SystemClockWrapper
    ) : BluetoothScoJob(logger, bluetoothScoHandler, systemClockWrapper) {

        override fun scoAction() {
            logger.d(TAG, "Attempting to disable bluetooth SCO")
            audioDeviceManager.enableBluetoothSco(false)
            headsetState = HeadsetState.Connected
        }

        override fun scoTimeOutAction() {
            headsetState = HeadsetState.AudioActivationError
        }
    }
}

internal class BluetoothHeadsetManager_v31plus

internal constructor(
    context: Context,
    logger: Logger,
    bluetoothAdapter: BluetoothAdapter,
    audioDeviceManager: AudioDeviceManager,
    headsetListener: BluetoothHeadsetConnectionListener? = null,
    bluetoothScoHandler: Handler = Handler(Looper.getMainLooper()),
    systemClockWrapper: SystemClockWrapper = SystemClockWrapper(),
    bluetoothIntentProcessor: BluetoothIntentProcessor = BluetoothIntentProcessorImpl(),
    headsetProxy: BluetoothHeadset? = null
) : BluetoothHeadsetManagerDefault(
    context,
    logger,
    bluetoothAdapter,
    audioDeviceManager,
    headsetListener,
    bluetoothScoHandler,
    systemClockWrapper,
    bluetoothIntentProcessor,
    headsetProxy
) {
    private val ERROR_MSG = "Bluetooth unsupported, permissions not granted"
    override fun start(headsetListener: BluetoothHeadsetConnectionListener) {
        if (hasPermissions()) {
            super.start(headsetListener)
        } else {
            logger.w(TAG, ERROR_MSG)
        }
    }

    override fun stop() {
        if (hasPermissions()) {
            super.stop()
        } else {
            logger.w(TAG, ERROR_MSG)
        }
    }

    override fun activate() {
        if (hasPermissions()) {
            super.activate()
        } else {
            logger.w(TAG, ERROR_MSG)
        }
    }

    override fun deactivate() {
        if (hasPermissions()) {
            super.deactivate()
        } else {
            logger.w(TAG, ERROR_MSG)
        }
    }

    override fun hasActivationError(): Boolean {
        if (hasPermissions()) {
            return super.hasActivationError()
        } else {
            logger.w(TAG, ERROR_MSG)
        }
        return false
    }

    override fun getHeadset(bluetoothHeadsetName: String?) : AudioDevice.BluetoothHeadset? {
        if (hasPermissions()) {
            return super.getHeadset(bluetoothHeadsetName)
        } else {
            logger.w(TAG, ERROR_MSG)
        }
        return null
    }

    private fun hasPermissions(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
    }
}
