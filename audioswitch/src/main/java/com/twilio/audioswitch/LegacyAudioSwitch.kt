package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.AudioDevice.Earpiece
import com.twilio.audioswitch.AudioDevice.Speakerphone
import com.twilio.audioswitch.AudioDevice.WiredHeadset
import com.twilio.audioswitch.android.Logger
import com.twilio.audioswitch.android.ProductionLogger
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.scanners.LegacyAudioDeviceScanner
import com.twilio.audioswitch.scanners.Scanner
import com.twilio.audioswitch.wired.WiredHeadsetReceiver

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
class LegacyAudioSwitch : AbstractAudioSwitch {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val headsetManager: BluetoothHeadsetManager?

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
        loggingEnabled: Boolean = true,
        audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {},
        preferredDeviceList: List<Class<out AudioDevice>> = defaultPreferredDeviceList
    ) : this(
        context, audioFocusChangeListener, ProductionLogger(loggingEnabled), preferredDeviceList
    )

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
        wiredHeadsetReceiver: WiredHeadsetReceiver = WiredHeadsetReceiver(context, logger),
        headsetManager: BluetoothHeadsetManager? = BluetoothHeadsetManager.newInstance(
            context,
            logger,
            BluetoothAdapter.getDefaultAdapter(),
            audioDeviceManager
        ),
        scanner: Scanner = LegacyAudioDeviceScanner(
            audioManager,
            audioDeviceManager,
            wiredHeadsetReceiver,
            headsetManager
        )
    ) : super(
        context,
        audioFocusChangeListener,
        scanner,
        logger.loggingEnabled,
        logger,
        preferredDeviceList,
        audioDeviceManager
    ) {
        this.headsetManager = headsetManager
    }

    override fun onDeviceDisconnected(audioDevice: AudioDevice) {
        val wasRemoved = if (audioDevice is BluetoothHeadset) {
            if (this.userSelectedAudioDevice is BluetoothHeadset) {
                this.userSelectedAudioDevice = null
            }
            this.availableUniqueAudioDevices.removeAll { it is BluetoothHeadset }
        } else {
            if (this.userSelectedAudioDevice == audioDevice) {
                this.userSelectedAudioDevice = null
            }
            this.availableUniqueAudioDevices.remove(audioDevice)
        }

        if (audioDevice is WiredHeadset && this.audioDeviceManager.hasEarpiece()) {
            this.availableUniqueAudioDevices.add(Earpiece())
        }
        this.selectAudioDevice(wasListChanged = wasRemoved)
    }

    override fun onActivate(audioDevice: AudioDevice) {
        when (audioDevice) {
            is BluetoothHeadset -> {
                this.audioDeviceManager.enableSpeakerphone(false)
                this.headsetManager?.activate()
            }
            is Earpiece, is WiredHeadset -> {
                this.audioDeviceManager.enableSpeakerphone(false)
                this.headsetManager?.deactivate()
            }
            is Speakerphone -> {
                this.audioDeviceManager.enableSpeakerphone(true)
                this.headsetManager?.deactivate()
            }
        }
    }

    override fun onDeactivate() {
        this.headsetManager?.deactivate()
    }
}
