package com.twilio.audioswitch

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AbstractAudioSwitch.State.*
import com.twilio.audioswitch.AudioDevice.*
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.android.ProductionLogger
import com.twilio.audioswitch.comparators.AudioDevicePriorityComparator
import com.twilio.audioswitch.scanners.Scanner
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

private const val TAG = "AudioSwitch"

/**
 * This class enables developers to enumerate available audio devices and select which device audio
 * should be routed to. It is strongly recommended that instances of this class are created and
 * accessed from a single application thread. Accessing an instance from multiple threads may cause
 * synchronization problems.
 *
 * @property loggingEnabled A property to configure AudioSwitch logging behavior. AudioSwitch logging is disabled by
 * default.
 * @property selectedAudioDevice Retrieves the selected [AudioDevice] from [AudioSwitch.selectDevice].
 * @property availableAudioDevices Retrieves the current list of available [AudioDevice]s.
 **/
abstract class AbstractAudioSwitch
/**
 * Constructs a new AudioSwitch instance.
 * - [context] - An Android Context.
 * - [loggingEnabled] - Toggle whether logging is enabled. This argument is false by default.
 * - [audioFocusChangeListener] - A listener that is invoked when the system audio focus is updated.
 * Note that updates are only sent to the listener after [activate] has been called.
 * - [preferredDeviceList] - The order in which [AudioSwitch] automatically selects and activates
 * an [AudioDevice]. This parameter is ignored if the [selectedAudioDevice] is not `null`.
 * The default preferred [AudioDevice] order is the following:
 * [BluetoothHeadset], [WiredHeadset], [Earpiece], [Speakerphone]
 * . The [preferredDeviceList] is added to the front of the default list. For example, if [preferredDeviceList]
 * is [Speakerphone] and [BluetoothHeadset], then the new preferred audio
 * device list will be:
 * [Speakerphone], [BluetoothHeadset], [WiredHeadset], [Earpiece].
 * An [IllegalArgumentException] is thrown if the [preferredDeviceList] contains duplicate [AudioDevice] elements.
 */ @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) internal constructor(
    context: Context,
    audioFocusChangeListener: OnAudioFocusChangeListener,
    scanner: Scanner,
    loggingEnabled: Boolean = true,
    private var logger: Logger = ProductionLogger(loggingEnabled),
    preferredDeviceList: List<Class<out AudioDevice>>,
    internal val audioDeviceManager: AudioDeviceManager = AudioDeviceManager(
        context,
        logger,
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
        audioFocusChangeListener = audioFocusChangeListener
    )
) : Scanner.Listener {
    internal var audioDeviceChangeListener: AudioDeviceChangeListener? = null
    internal var state: State = STOPPED
    private val deviceScanner: Scanner = scanner
    private val preferredDeviceList: List<Class<out AudioDevice>>
    protected var userSelectedAudioDevice: AudioDevice? = null

    internal enum class State {
        STARTED, ACTIVATED, STOPPED
    }

    var loggingEnabled: Boolean
        get() = logger.loggingEnabled
        set(value) {
            logger.loggingEnabled = value
        }
    var selectedAudioDevice: AudioDevice? = null
        private set
    val availableUniqueAudioDevices: SortedSet<AudioDevice>
    val availableAudioDevices: List<AudioDevice>
        get() = this.availableUniqueAudioDevices.toList()

    init {
        this.preferredDeviceList = getPreferredDeviceList(preferredDeviceList)
        this.availableUniqueAudioDevices =
            ConcurrentSkipListSet(AudioDevicePriorityComparator(this.preferredDeviceList))
        logger.d(TAG, "AudioSwitch($VERSION)")
        logger.d(TAG, "Preferred device list = ${this.preferredDeviceList.map { it.simpleName }}")
    }

    private fun getPreferredDeviceList(preferredDeviceList: List<Class<out AudioDevice>>):
            List<Class<out AudioDevice>> {
        require(hasNoDuplicates(preferredDeviceList))

        return if (preferredDeviceList.isEmpty() || preferredDeviceList == defaultPreferredDeviceList) {
            defaultPreferredDeviceList
        } else {
            val result = defaultPreferredDeviceList.toMutableList()
            result.removeAll(preferredDeviceList)
            preferredDeviceList.forEachIndexed { index, device ->
                result.add(index, device)
            }
            result
        }
    }

    override fun onDeviceConnected(audioDevice: AudioDevice) {
        this.availableUniqueAudioDevices.add(audioDevice)
        this.selectAudioDevice()
    }

    /**
     * Starts listening for audio device changes and calls the [listener] upon each change.
     * **Note:** When audio device listening is no longer needed, [AudioSwitch.stop] should be
     * called in order to prevent a memory leak.
     */
    fun start(listener: AudioDeviceChangeListener) {
        audioDeviceChangeListener = listener
        when (state) {
            STOPPED -> {
                this.deviceScanner.start(this)
                state = STARTED
            }
            else -> {
                logger.d(TAG, "Redundant start() invocation while already in the started or activated state")
            }
        }
    }

    /**
     * Stops listening for audio device changes if [AudioSwitch.start] has already been
     * invoked. [AudioSwitch.deactivate] will also get called if a device has been activated
     * with [AudioSwitch.activate].
     */
    fun stop() {
        when (state) {
            ACTIVATED -> {
                deactivate()
                closeListeners()
            }
            STARTED -> {
                closeListeners()
            }
            STOPPED -> {
                logger.d(TAG, "Redundant stop() invocation while already in the stopped state")
            }
        }
    }

    /**
     * Performs audio routing and unmuting on the selected device from
     * [AudioSwitch.selectDevice]. Audio focus is also acquired for the client application.
     * **Note:** [AudioSwitch.deactivate] should be invoked to restore the prior audio
     * state.
     */
    fun activate() {
        when (state) {
            STARTED -> {
                audioDeviceManager.cacheAudioState()

                // Always set mute to false for WebRTC
                audioDeviceManager.mute(false)
                audioDeviceManager.setAudioFocus()
                selectedAudioDevice?.let { this.onActivate(it) }
                state = ACTIVATED
            }
            ACTIVATED -> selectedAudioDevice?.let { this.onActivate(it) }
            STOPPED -> throw IllegalStateException()
        }
    }

    /**
     * Restores the audio state prior to calling [AudioSwitch.activate] and removes
     * audio focus from the client application.
     */
    fun deactivate() {
        when (state) {
            ACTIVATED -> {
                this.onDeactivate()
                // Restore stored audio state
                audioDeviceManager.restoreAudioState()
                state = STARTED
            }
            STARTED, STOPPED -> {
            }
        }
    }

    /**
     * Selects the desired [audioDevice]. If the provided [AudioDevice] is not
     * available, no changes are made. If the provided device is null, one is chosen based on the
     * specified preferred device list or the following default list:
     * [BluetoothHeadset], [WiredHeadset], [Earpiece], [Speakerphone].
     */
    fun selectDevice(audioDevice: AudioDevice?) {
        logger.d(TAG, "Selected AudioDevice = $audioDevice")
        userSelectedAudioDevice = audioDevice

        this.selectAudioDevice(audioDevice)
    }

    protected fun selectAudioDevice(audioDevice: AudioDevice? = this.getBestDevice()) {
        if (selectedAudioDevice == audioDevice) {
            return
        }

        // Select the audio device
        logger.d(TAG, "Current user selected AudioDevice = $userSelectedAudioDevice")
        selectedAudioDevice = audioDevice

        // Activate the device if in the active state
        if (state == ACTIVATED) {
            activate()
        }
        // trigger audio device change listener if there has been a change

        audioDeviceChangeListener?.invoke(availableUniqueAudioDevices.toList(), selectedAudioDevice)
    }

    private fun getBestDevice(): AudioDevice? {
        val userSelectedAudioDevice = userSelectedAudioDevice
        return if (userSelectedAudioDevice != null && this.deviceScanner.isDeviceActive(userSelectedAudioDevice)) {
            userSelectedAudioDevice
        } else {
            val device = this.availableUniqueAudioDevices.firstOrNull {
                this.deviceScanner.isDeviceActive(it)
            }
            device
        }
    }

    private fun hasNoDuplicates(list: List<Class<out AudioDevice>>) =
        list.groupingBy { it }.eachCount().filter { it.value > 1 }.isEmpty()

    private fun closeListeners() {
        this.deviceScanner.stop()
        audioDeviceChangeListener = null
        state = STOPPED
    }

    protected abstract fun onActivate(audioDevice: AudioDevice)
    protected abstract fun onDeactivate()

    companion object {
        /**
         * The version of the AudioSwitch library.
         */
        const val VERSION = BuildConfig.VERSION_NAME

        internal val defaultPreferredDeviceList by lazy {
            listOf(
                BluetoothHeadset::class.java,
                WiredHeadset::class.java,
                Earpiece::class.java,
                Speakerphone::class.java,
            )
        }
    }
}