//[audioswitch](../../index.md)/[com.twilio.audioswitch](../index.md)/[AudioSwitch](index.md)/[AudioSwitch](-audio-switch.md)



# AudioSwitch  
[androidJvm]  
Brief description  


Constructs a new AudioSwitch instance.



## Parameters  
  
androidJvm  
  
|  Name|  Summary| 
|---|---|
| audioFocusChangeListener| <br><br>A listener that is invoked when the system audio focus is updated. Note that updates are only sent to the listener after [activate](activate.md) has been called.<br><br>
| context| <br><br>An Android Context.<br><br>
| loggingEnabled| <br><br>Toggle whether logging is enabled. This argument is false by default.<br><br>
| preferredDeviceList| <br><br><br><br>The order in which [AudioSwitch](index.md) automatically selects and activates an [AudioDevice](../-audio-device/index.md). This parameter is ignored if the [selectedAudioDevice](index.md#com.twilio.audioswitch/AudioSwitch/selectedAudioDevice/#/PointingToDeclaration/) is not null.<br><br><br><br>The default preferred [AudioDevice](../-audio-device/index.md) order is the following:<br><br><br><br>[BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md), [Speakerphone](../-audio-device/-speakerphone/index.md)<br><br><br><br>preferredDeviceList is added to the front of the default list. For example, if preferredDeviceList is [Speakerphone](../-audio-device/-speakerphone/index.md) and [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), then the new preferred audio device list will be:<br><br><br><br>[Speakerphone](../-audio-device/-speakerphone/index.md), [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md).<br><br><br><br>
  
  
Content  
@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  
  
fun [AudioSwitch](-audio-switch.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), loggingEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), audioFocusChangeListener: [AudioManager.OnAudioFocusChangeListener](https://developer.android.com/reference/kotlin/android/media/AudioManager.OnAudioFocusChangeListener.html), preferredDeviceList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)<out [AudioDevice](../-audio-device/index.md)>>)  



