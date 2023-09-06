package com.twilio.audioswitch

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AbstractAudioSwitch.State.*
import com.twilio.audioswitch.AudioDevice.*
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.android.ProductionLogger
import com.twilio.audioswitch.scanners.AudioDeviceScanner
import com.twilio.audioswitch.scanners.Scanner
import java.util.*

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
@RequiresApi(Build.VERSION_CODES.M)
class AudioSwitch : AbstractAudioSwitch {
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
     */
    @JvmOverloads
    constructor(
        context: Context,
        loggingEnabled: Boolean = false,
        audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {},
        preferredDeviceList: List<Class<out AudioDevice>> = defaultPreferredDeviceList
    ) : this(
        context, audioFocusChangeListener, ProductionLogger(loggingEnabled), preferredDeviceList
    )

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
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal constructor(
        context: Context,
        audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener,
        logger: Logger,
        preferredDeviceList: List<Class<out AudioDevice>>,
        audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
        audioDeviceManager: AudioDeviceManager = AudioDeviceManager(
            context,
            logger,
            audioManager,
            audioFocusChangeListener = audioFocusChangeListener
        ),
        handler: Handler = Handler(Looper.getMainLooper()),
        scanner: Scanner = AudioDeviceScanner(audioManager, handler),
    ) : super(
        context = context,
        audioFocusChangeListener = audioFocusChangeListener,
        scanner = scanner,
        logger = logger,
        preferredDeviceList = preferredDeviceList,
        audioDeviceManager = audioDeviceManager,
    )

    override fun onDeviceDisconnected(audioDevice: AudioDevice) {
        this.logger.d(TAG_AUDIO_SWITCH, "onDeviceDisconnected($audioDevice)")
        var wasChanged = this.availableUniqueAudioDevices.remove(audioDevice)
        if (this.userSelectedAudioDevice == audioDevice) {
            this.userSelectedAudioDevice = null
        }

        if (audioDevice is WiredHeadset && this.audioDeviceManager.hasEarpiece()) {
            wasChanged = this.availableUniqueAudioDevices.add(Earpiece()) || wasChanged
        }
        this.selectAudioDevice(wasChanged)
    }

    override fun onActivate(audioDevice: AudioDevice) {
        this.logger.d(TAG_AUDIO_SWITCH, "onActivate($audioDevice)")

        when (audioDevice) {
            is BluetoothHeadset -> {
                this.audioDeviceManager.enableSpeakerphone(false)
                this.audioDeviceManager.enableBluetoothSco(true)
            }

            is Earpiece, is WiredHeadset -> {
                this.audioDeviceManager.enableSpeakerphone(false)
                this.audioDeviceManager.enableBluetoothSco(false)
            }

            is Speakerphone -> {
                this.audioDeviceManager.enableBluetoothSco(false)
                this.audioDeviceManager.enableSpeakerphone(true)
            }
        }
    }

    override fun onDeactivate() {
        this.logger.d(TAG_AUDIO_SWITCH, "onDeactivate")
    }
}
