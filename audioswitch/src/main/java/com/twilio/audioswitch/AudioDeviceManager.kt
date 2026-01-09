package com.twilio.audioswitch

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Build
import androidx.annotation.RequiresApi
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.Logger

private const val TAG = "AudioDeviceManager"

internal class AudioDeviceManager(
    private val context: Context,
    private val logger: Logger,
    private val audioManager: AudioManager,
    private val build: BuildWrapper = BuildWrapper(),
    private val audioFocusRequest: AudioFocusRequestWrapper = AudioFocusRequestWrapper(),
    private val audioFocusChangeListener: OnAudioFocusChangeListener,
) {
    private var savedAudioMode = 0
    private var savedIsMicrophoneMuted = false
    private var savedSpeakerphoneEnabled = false
    private var audioRequest: AudioFocusRequest? = null

    @SuppressLint("NewApi")
    fun hasEarpiece(): Boolean {
        var hasEarpiece = false
        if (build.getVersion() >= Build.VERSION_CODES.M &&
            context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        ) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
                    logger.d(TAG, "Builtin Earpiece available")
                    hasEarpiece = true
                }
            }
        } else {
            hasEarpiece = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }
        return hasEarpiece
    }

    @SuppressLint("NewApi")
    fun hasSpeakerphone(): Boolean {
        return if (build.getVersion() >= Build.VERSION_CODES.M &&
            context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
        ) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    logger.d(TAG, "Speakerphone available")
                    return true
                }
            }
            false
        } else {
            logger.d(TAG, "Speakerphone available")
            true
        }
    }

    @SuppressLint("NewApi")
    fun setAudioFocus() {
        // Request audio focus before making any device switch.
        if (build.getVersion() >= Build.VERSION_CODES.O) {
            audioRequest = audioFocusRequest.buildRequest(audioFocusChangeListener)
            audioRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            )
        }
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance. Some devices have difficulties with speaker mode
         * if this is not set.
         */
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    @SuppressLint("NewApi")
    fun enableBluetoothSco(enable: Boolean) {
        if (build.getVersion() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            enableCommunicationForAudioDeviceType(AudioDeviceInfo.TYPE_BLUETOOTH_SCO, enable)
        } else {
            audioManager.run { if (enable) startBluetoothSco() else stopBluetoothSco() }
        }
    }

    @SuppressLint("NewApi")
    fun enableSpeakerphone(enable: Boolean) {
        var speakerEnabled: Boolean
        if (build.getVersion() >= Build.VERSION_CODES.S) {
            speakerEnabled = audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            enableCommunicationForAudioDeviceType(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, enable)
        } else {
            audioManager.isSpeakerphoneOn = enable
            speakerEnabled = audioManager.isSpeakerphoneOn
        }
        /**
         * Some Samsung devices (reported Galaxy s9, s21) fail to route audio through USB headset
         * when in MODE_IN_COMMUNICATION & when running unit tests, Build.MODEL is null
         */
        if (!speakerEnabled && null != Build.MODEL &&
            "^SM-G(960|99)".toRegex().containsMatchIn(Build.MODEL)
        ) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    audioManager.mode = AudioManager.MODE_NORMAL
                    break
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun enableEarpiece(enable: Boolean) {
        if (build.getVersion() >= Build.VERSION_CODES.S) {
            enableCommunicationForAudioDeviceType(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE, enable)
        }
    }

    fun mute(mute: Boolean) {
        audioManager.isMicrophoneMute = mute
    }

    // TODO Consider persisting audio state in the event of process death
    @SuppressLint("NewApi")
    fun cacheAudioState() {
        savedAudioMode = audioManager.mode
        savedIsMicrophoneMuted = audioManager.isMicrophoneMute

        if (build.getVersion() >= Build.VERSION_CODES.S) {
            val currentDevice = audioManager.communicationDevice
            savedSpeakerphoneEnabled = currentDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        } else {
            savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn
        }
    }

    @SuppressLint("NewApi")
    fun restoreAudioState() {
        audioManager.mode = savedAudioMode
        mute(savedIsMicrophoneMuted)
        enableSpeakerphone(savedSpeakerphoneEnabled)
        if (build.getVersion() >= Build.VERSION_CODES.O) {
            audioRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun enableCommunicationForAudioDeviceType(
        deviceType: Int,
        enable: Boolean,
    ) {
        if (enable) {
            audioManager.availableCommunicationDevices
                .firstOrNull { it.type == deviceType }
                ?.let { device ->
                    if (device != audioManager.communicationDevice) {
                        audioManager.setCommunicationDevice(device)
                    }
                }
        } else if (audioManager.communicationDevice?.type == deviceType) {
            audioManager.clearCommunicationDevice()
        }
    }
}
