package com.twilio.audioswitch.scanners

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.twilio.audioswitch.AudioDevice
import java.util.concurrent.atomic.AtomicReference

@RequiresApi(Build.VERSION_CODES.M)
internal class AudioDeviceScanner(
    private val audioManager: AudioManager,
    private val handler: Handler,
) : AudioDeviceCallback(), Scanner {
    private val listener = AtomicReference<Scanner.Listener>(null)

    override fun isDeviceActive(audioDevice: AudioDevice): Boolean =
        this.audioManager
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any {
                val audioDevice1 = it.isAudioDevice(audioDevice)
                audioDevice1
            }

    override fun start(listener: Scanner.Listener): Boolean {
        this.listener.set(listener)
        this.audioManager.registerAudioDeviceCallback(this, this.handler)
        return true
    }

    override fun stop(): Boolean {
        this.audioManager.unregisterAudioDeviceCallback(this)
        return true
    }

    override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
        super.onAudioDevicesAdded(addedDevices)
        addedDevices?.mapNotNull {
            it.audioDevice
        }
            ?.toSet()
            ?.forEach {
                this.listener.get().onDeviceConnected(it)
            }
    }

    override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
        super.onAudioDevicesRemoved(removedDevices)
        removedDevices?.mapNotNull {
            it.audioDevice
        }
            ?.toSet()
            ?.forEach {
                this.listener.get().onDeviceDisconnected(it)
            }
    }

    val AudioDeviceInfo.audioDevice: AudioDevice?
        get() = if (this.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || this.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
            AudioDevice.BluetoothHeadset(this.productName.toString())
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (this.type == AudioDeviceInfo.TYPE_BLE_HEADSET || this.type == AudioDeviceInfo.TYPE_BLE_SPEAKER)) {
            AudioDevice.BluetoothHeadset(this.productName.toString())
        } else if (this.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || this.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || this.type == AudioDeviceInfo.TYPE_USB_HEADSET) {
            AudioDevice.WiredHeadset()
        } else if (this.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE) {
            AudioDevice.Earpiece()
        } else if (this.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
            AudioDevice.Speakerphone()
        } else {
            null
        }

    fun AudioDeviceInfo.isAudioDevice(audioDevice: AudioDevice): Boolean =
        when (audioDevice) {
            is AudioDevice.BluetoothHeadset ->
                if (this.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || this.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
                    true
                } else {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (this.type == AudioDeviceInfo.TYPE_BLE_HEADSET || this.type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
                }
            is AudioDevice.Earpiece ->
                this.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
            is AudioDevice.Speakerphone ->
                this.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            is AudioDevice.WiredHeadset ->
                if (this.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || this.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                    true
                } else {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.type == AudioDeviceInfo.TYPE_USB_HEADSET
                }
        }
}