package com.twilio.audioswitch

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AbstractAudioSwitch.State.*
import com.twilio.audioswitch.AudioDevice.*
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.android.ProductionLogger
import com.twilio.audioswitch.comparators.AudioDevicePriorityComparator
import com.twilio.audioswitch.scanners.Scanner
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

internal const val TAG_AUDIO_SWITCH = "AudioSwitch"

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
    internal var logger: Logger = ProductionLogger(loggingEnabled),
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val deviceScanner: Scanner = scanner
    private var preferredDeviceList: List<Class<out AudioDevice>>
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
    var availableUniqueAudioDevices: SortedSet<AudioDevice>
        private set
    val availableAudioDevices: List<AudioDevice>
        get() = this.availableUniqueAudioDevices.toList()

    /**
     * When true, AudioSwitch will request audio focus upon activation and abandon upon deactivation.
     *
     * Defaults to true.
     */
    var manageAudioFocus = true

    /**
     * The audio mode to use when requesting audio focus.
     *
     * Defaults to [AudioManager.MODE_IN_COMMUNICATION].
     *
     * @see AudioManager.MODE_NORMAL
     */
    var audioMode: Int
        get() = this.audioDeviceManager.audioMode
        set(value) {
            this.audioDeviceManager.audioMode = value
        }

    /**
     * The focus mode to use when requesting audio focus.
     *
     * Defaults to [AudioManager.AUDIOFOCUS_GAIN_TRANSIENT].
     *
     * @see AudioManager.AUDIOFOCUS_GAIN
     */
    var focusMode: Int
        get() = this.audioDeviceManager.focusMode
        set(value) {
            this.audioDeviceManager.focusMode = value
        }

    /**
     * The audio stream type to use when requesting audio focus on pre-O devices.
     *
     * Defaults to [AudioManager.STREAM_VOICE_CALL].
     *
     * Refer to this [compatibility table](https://source.android.com/docs/core/audio/attributes#compatibility)
     * to ensure that your values match between android versions.
     */
    var audioStreamType: Int
        get() = this.audioDeviceManager.audioStreamType
        set(value) {
            this.audioDeviceManager.audioStreamType = value
        }

    /**
     * The audio attribute usage type to use when requesting audio focus on devices O and beyond.
     *
     * Defaults to [AudioAttributes.USAGE_VOICE_COMMUNICATION].
     *
     * Refer to this [compatibility table](https://source.android.com/docs/core/audio/attributes#compatibility)
     * to ensure that your values match between android versions.
     */
    var audioAttributeUsageType: Int
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        get() = this.audioDeviceManager.audioAttributeUsageType
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(value) {
            this.audioDeviceManager.audioAttributeUsageType = value
        }

    /**
     * The audio attribute content type to use when requesting audio focus on devices O and beyond.
     *
     * Defaults to [AudioAttributes.CONTENT_TYPE_SPEECH].
     *
     * Refer to this [compatibility table](https://source.android.com/docs/core/audio/attributes#compatibility)
     * to ensure that your values match between android versions.
     */
    var audioAttributeContentType: Int
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        get() = this.audioDeviceManager.audioAttributeContentType
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(value) {
            this.audioDeviceManager.audioAttributeContentType = value
        }

    /**
     * On certain Android devices, audio routing does not function properly and bluetooth SCO will not work
     * unless audio mode is set to [AudioManager.MODE_IN_COMMUNICATION] or [AudioManager.MODE_IN_CALL].
     *
     * AudioSwitch by default will not handle audio routing in those cases to avoid audio issues.
     *
     * If this set to true, AudioSwitch will attempt to do audio routing, though behavior is undefined.
     */
    var forceHandleAudioRouting = false

    init {
        this.preferredDeviceList = getPreferredDeviceList(preferredDeviceList)
        this.availableUniqueAudioDevices =
            ConcurrentSkipListSet(AudioDevicePriorityComparator(this.preferredDeviceList))
        logger.d(TAG_AUDIO_SWITCH, "AudioSwitch($VERSION)")
        logger.d(TAG_AUDIO_SWITCH, "Preferred device list = ${this.preferredDeviceList.map { it.simpleName }}")
    }

    fun setPreferredDeviceList(preferredDeviceList: List<Class<out AudioDevice>>) {
        if (preferredDeviceList == this.preferredDeviceList) {
            return
        }

        val oldAvailableDevices = availableUniqueAudioDevices

        this.preferredDeviceList = getPreferredDeviceList(preferredDeviceList)
        this.availableUniqueAudioDevices =
            ConcurrentSkipListSet(AudioDevicePriorityComparator(this.preferredDeviceList))

        availableUniqueAudioDevices.addAll(oldAvailableDevices)

        logger.d(TAG_AUDIO_SWITCH, "New preferred device list = ${this.preferredDeviceList.map { it.simpleName }}")

        selectAudioDevice(wasListChanged = false)
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
        this.logger.d(TAG_AUDIO_SWITCH, "onDeviceConnected($audioDevice)")
        if (audioDevice is Earpiece && this.availableAudioDevices.contains(WiredHeadset())) {
            return
        }
        val wasAdded = this.availableUniqueAudioDevices.add(audioDevice)
        if (audioDevice is WiredHeadset) {
            this.availableUniqueAudioDevices.removeAll { it is Earpiece }
        }
        this.selectAudioDevice(wasListChanged = wasAdded)
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
                logger.d(TAG_AUDIO_SWITCH, "Redundant start() invocation while already in the started or activated state")
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
                logger.d(TAG_AUDIO_SWITCH, "Redundant stop() invocation while already in the stopped state")
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
                if (manageAudioFocus) {
                    audioDeviceManager.setAudioFocus()
                }
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
        logger.d(TAG_AUDIO_SWITCH, "Selected AudioDevice = $audioDevice")
        userSelectedAudioDevice = audioDevice

        this.selectAudioDevice(wasListChanged = false, audioDevice = audioDevice)
    }

    protected fun shouldHandleAudioRouting(): Boolean {
        val audioMode = this.audioMode

        return forceHandleAudioRouting || audioMode == AudioManager.MODE_IN_COMMUNICATION || audioMode == AudioManager.MODE_IN_CALL
    }

    protected fun selectAudioDevice(wasListChanged: Boolean, audioDevice: AudioDevice? = this.getBestDevice()) {

        if (selectedAudioDevice == audioDevice) {
            if (wasListChanged) {
                audioDeviceChangeListener?.invoke(availableUniqueAudioDevices.toList(), selectedAudioDevice)
            }
            return
        }

        // Select the audio device
        if (shouldHandleAudioRouting()) {
            logger.d(TAG_AUDIO_SWITCH, "Current user selected AudioDevice = $userSelectedAudioDevice")
            selectedAudioDevice = audioDevice

            // Activate the device if in the active state
            if (state == ACTIVATED) {
                activate()
            }
        }
        // trigger audio device change listener if there has been a change
        audioDeviceChangeListener?.invoke(availableUniqueAudioDevices.toList(), selectedAudioDevice)
    }

    private fun getBestDevice(): AudioDevice? {
        val userSelectedAudioDevice = userSelectedAudioDevice
        return if (userSelectedAudioDevice != null && this.deviceScanner.isDeviceActive(userSelectedAudioDevice)) {
            userSelectedAudioDevice
        } else {
            this.availableUniqueAudioDevices.firstOrNull {
                this.deviceScanner.isDeviceActive(it)
            }
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