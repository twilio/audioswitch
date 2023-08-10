package com.twilio.audioswitch

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener

internal class AudioFocusRequestWrapper {

    @SuppressLint("NewApi")
    fun buildRequest(
        audioFocusChangeListener: OnAudioFocusChangeListener,
        focusMode: Int = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
        audioAttributeUsageType: Int = AudioAttributes.USAGE_VOICE_COMMUNICATION,
        audioAttributeContentType: Int = AudioAttributes.CONTENT_TYPE_SPEECH
    ): AudioFocusRequest {
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(audioAttributeUsageType)
            .setContentType(audioAttributeContentType)
            .build()
        return AudioFocusRequest.Builder(focusMode)
            .setAudioAttributes(playbackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }
}
