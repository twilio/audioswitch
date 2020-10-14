---
title: AudioSwitch -
---
//[audioswitch](../../index.md)/[com.twilio.audioswitch](../index.md)/[AudioSwitch](index.md)/[AudioSwitch](-audio-switch.md)



# AudioSwitch  
[androidJvm]  
Brief description  


Constructs a new AudioSwitch instance.<br><br> **context** - An Android Context.<br> [**loggingEnabled**](index.md#com.twilio.audioswitch/AudioSwitch/loggingEnabled/#/PointingToDeclaration/) - Toggle whether logging is enabled. This argument is false by default.<br> **audioFocusChangeListener** - A listener that is invoked when the system audio focus is updated. Note that updates are only sent to the listener after [activate](activate.md) has been called.<br> **preferredDeviceList** - The order in which [AudioSwitch](index.md) automatically selects and activates an [AudioDevice](../-audio-device/index.md). This parameter is ignored if the [selectedAudioDevice](index.md#com.twilio.audioswitch/AudioSwitch/selectedAudioDevice/#/PointingToDeclaration/) is not null. The default preferred [AudioDevice](../-audio-device/index.md) order is the following: [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md), [Speakerphone](../-audio-device/-speakerphone/index.md) . The preferredDeviceList is added to the front of the default list. For example, if preferredDeviceList is [Speakerphone](../-audio-device/-speakerphone/index.md) and [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), then the new preferred audio device list will be: [Speakerphone](../-audio-device/-speakerphone/index.md), [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md). An [IllegalArgumentException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-argument-exception/index.html) is thrown if the preferredDeviceList contains duplicate [AudioDevice](../-audio-device/index.md) elements.

  
Content  
@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  
  
fun [AudioSwitch](-audio-switch.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), loggingEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), audioFocusChangeListener: [AudioManager.OnAudioFocusChangeListener](https://developer.android.com/reference/kotlin/android/media/AudioManager.OnAudioFocusChangeListener.html), preferredDeviceList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[Class](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)<out [AudioDevice](../-audio-device/index.md)>>)  



