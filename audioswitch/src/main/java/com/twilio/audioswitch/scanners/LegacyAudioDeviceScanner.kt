package com.twilio.audioswitch.scanners

import android.media.AudioManager
import androidx.annotation.VisibleForTesting
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioDeviceManager
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetConnectionListener
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetManager
import com.twilio.audioswitch.wired.WiredDeviceConnectionListener
import com.twilio.audioswitch.wired.WiredHeadsetReceiver
import java.util.concurrent.atomic.AtomicReference

internal class LegacyAudioDeviceScanner(
    private val audioManager: AudioManager,
    private val audioDeviceManager: AudioDeviceManager,
    private val wiredHeadsetReceiver: WiredHeadsetReceiver,
    private val bluetoothHeadsetManager: BluetoothHeadsetManager?,
) : Scanner {
    private val listener = AtomicReference<Scanner.Listener>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val bluetoothDeviceConnectionListener = object : BluetoothHeadsetConnectionListener {
        private val connectedDevices = AtomicReference<AudioDevice.BluetoothHeadset?>()

        @Synchronized
        override fun onBluetoothHeadsetStateChanged(headsetName: String?) {
            val listener = this@LegacyAudioDeviceScanner.listener.get()

            if (headsetName == null) {
                val bluetoothHeadset = this.connectedDevices.get()
                val newBluetoothHeadset = this@LegacyAudioDeviceScanner.bluetoothHeadsetManager?.getHeadset(null)
                if (newBluetoothHeadset == bluetoothHeadset) {
                    return
                }

                this.connectedDevices.set(newBluetoothHeadset)
                bluetoothHeadset?.let { listener.onDeviceDisconnected(it) }
                newBluetoothHeadset?.let { listener.onDeviceConnected(it) }
            } else {
                val audioDevice = AudioDevice.BluetoothHeadset(headsetName)
                this.connectedDevices.set(audioDevice)
                listener.onDeviceConnected(audioDevice)
            }
        }

        @Synchronized
        override fun onBluetoothHeadsetActivationError() {
            val audioDevice = AudioDevice.BluetoothHeadset("Bluetooth")
            this@LegacyAudioDeviceScanner
                .listener
                .get()
                .onDeviceDisconnected(audioDevice)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val wiredDeviceConnectionListener = object : WiredDeviceConnectionListener {
        private val audioDevice = AudioDevice.WiredHeadset()
        override fun onDeviceConnected() {
            this@LegacyAudioDeviceScanner
                .listener
                .get()
                .onDeviceConnected(this.audioDevice)
        }

        override fun onDeviceDisconnected() {
            this@LegacyAudioDeviceScanner
                .listener
                .get()
                .onDeviceDisconnected(this.audioDevice)
        }
    }

    override fun isDeviceActive(audioDevice: AudioDevice): Boolean =
        when (audioDevice) {
            is AudioDevice.BluetoothHeadset ->
                (this.bluetoothHeadsetManager?.hasActivationError() == false) &&
                        (this.bluetoothHeadsetManager.getHeadset(audioDevice.name) != null)
            is AudioDevice.Earpiece ->
                true
            is AudioDevice.Speakerphone ->
                this.audioManager.isSpeakerphoneOn
            is AudioDevice.WiredHeadset ->
                this.audioManager.isWiredHeadsetOn
        }

    override fun start(listener: Scanner.Listener): Boolean {
        this.listener.set(listener)
        bluetoothHeadsetManager?.start(bluetoothDeviceConnectionListener)
        wiredHeadsetReceiver.start(wiredDeviceConnectionListener)

        if (this.audioDeviceManager.hasEarpiece()) {
            listener.onDeviceConnected(AudioDevice.Earpiece())
        }
        if (this.audioDeviceManager.hasSpeakerphone()) {
            listener.onDeviceConnected(AudioDevice.Speakerphone())
        }
        return true
    }

    override fun stop(): Boolean {
        bluetoothHeadsetManager?.stop()
        wiredHeadsetReceiver.stop()
        return true
    }
}