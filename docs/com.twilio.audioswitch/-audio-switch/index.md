---
title: AudioSwitch -
---
//[audioswitch](../../index.md)/[com.twilio.audioswitch](../index.md)/[AudioSwitch](index.md)



# AudioSwitch  
 [androidJvm] 

This class enables developers to enumerate available audio devices and select which device audio should be routed to. It is strongly recommended that instances of this class are created and accessed from a single application thread. Accessing an instance from multiple threads may cause synchronization problems.

class [AudioSwitch](index.md)   


## Constructors  
  
|  Name|  Summary| 
|---|---|
| [AudioSwitch](-audio-switch.md)|  [androidJvm] <br><br><br><br>Constructs a new AudioSwitch instance.<br><br><ul><li>context - An Android Context.</li><li>[loggingEnabled](index.md#com.twilio.audioswitch/AudioSwitch/loggingEnabled/#/PointingToDeclaration/) - Toggle whether logging is enabled. This argument is false by default.</li><li>audioFocusChangeListener - A listener that is invoked when the system audio focus is updated. Note that updates are only sent to the listener after [activate](activate.md) has been called.</li><li>preferredDeviceList - The order in which [AudioSwitch](index.md) automatically selects and activates an [AudioDevice](../-audio-device/index.md). This parameter is ignored if the [selectedAudioDevice](index.md#com.twilio.audioswitch/AudioSwitch/selectedAudioDevice/#/PointingToDeclaration/) is not null. The default preferred [AudioDevice](../-audio-device/index.md) order is the following: [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md), [Speakerphone](../-audio-device/-speakerphone/index.md) . The preferredDeviceList is added to the front of the default list. For example, if preferredDeviceList is [Speakerphone](../-audio-device/-speakerphone/index.md) and [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), then the new preferred audio device list will be: [Speakerphone](../-audio-device/-speakerphone/index.md), [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md). An [IllegalArgumentException](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-illegal-argument-exception/index.html) is thrown if the preferredDeviceList contains duplicate [AudioDevice](../-audio-device/index.md) elements.</li></ul><br><br>@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  <br>  <br>fun [AudioSwitch](-audio-switch.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), loggingEnabled: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html), audioFocusChangeListener: [AudioManager.OnAudioFocusChangeListener](https://developer.android.com/reference/kotlin/android/media/AudioManager.OnAudioFocusChangeListener.html), preferredDeviceList: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)<out [AudioDevice](../-audio-device/index.md)>>)   <br>


## Types  
  
|  Name|  Summary| 
|---|---|
| [Companion](-companion/index.md)| [androidJvm]  <br>Content  <br>object [Companion](-companion/index.md)  <br><br><br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| [activate](activate.md)| [androidJvm]  <br>Brief description  <br><br><br>Performs audio routing and unmuting on the selected device from [AudioSwitch.selectDevice](select-device.md). Audio focus is also acquired for the client application. **Note:**[AudioSwitch.deactivate](deactivate.md) should be invoked to restore the prior audio state.<br><br>  <br>Content  <br>fun [activate](activate.md)()  <br><br><br>
| [deactivate](deactivate.md)| [androidJvm]  <br>Brief description  <br><br><br>Restores the audio state prior to calling [AudioSwitch.activate](activate.md) and removes audio focus from the client application.<br><br>  <br>Content  <br>fun [deactivate](deactivate.md)()  <br><br><br>
| [equals](-companion/index.md#kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open operator override fun [equals](-companion/index.md#kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](-companion/index.md#kotlin/Any/hashCode/#/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open override fun [hashCode](-companion/index.md#kotlin/Any/hashCode/#/PointingToDeclaration/)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [selectDevice](select-device.md)| [androidJvm]  <br>Brief description  <br><br><br>Selects the desired audioDevice. If the provided [AudioDevice](../-audio-device/index.md) is not available, no changes are made. If the provided device is null, one is chosen based on the specified preferred device list or the following default list: [BluetoothHeadset](../-audio-device/-bluetooth-headset/index.md), [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md), [Speakerphone](../-audio-device/-speakerphone/index.md).<br><br>  <br>Content  <br>fun [selectDevice](select-device.md)(audioDevice: [AudioDevice](../-audio-device/index.md)?)  <br><br><br>
| [start](start.md)| [androidJvm]  <br>Brief description  <br><br><br>Starts listening for audio device changes and calls the listener upon each change. **Note:** When audio device listening is no longer needed, [AudioSwitch.stop](stop.md) should be called in order to prevent a memory leak.<br><br>  <br>Content  <br>fun [start](start.md)(listener: [AudioDeviceChangeListener](../index.md#com.twilio.audioswitch/AudioDeviceChangeListener///PointingToDeclaration/))  <br><br><br>
| [stop](stop.md)| [androidJvm]  <br>Brief description  <br><br><br>Stops listening for audio device changes if [AudioSwitch.start](start.md) has already been invoked. [AudioSwitch.deactivate](deactivate.md) will also get called if a device has been activated with [AudioSwitch.activate](activate.md).<br><br>  <br>Content  <br>fun [stop](stop.md)()  <br><br><br>
| [toString](-companion/index.md#kotlin/Any/toString/#/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open override fun [toString](-companion/index.md#kotlin/Any/toString/#/PointingToDeclaration/)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [availableAudioDevices](index.md#com.twilio.audioswitch/AudioSwitch/availableAudioDevices/#/PointingToDeclaration/)|  [androidJvm] <br><br>Retrieves the current list of available [AudioDevice](../-audio-device/index.md)s.<br><br>val [availableAudioDevices](index.md#com.twilio.audioswitch/AudioSwitch/availableAudioDevices/#/PointingToDeclaration/): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[AudioDevice](../-audio-device/index.md)>   <br>
| [loggingEnabled](index.md#com.twilio.audioswitch/AudioSwitch/loggingEnabled/#/PointingToDeclaration/)|  [androidJvm] <br><br>A property to configure AudioSwitch logging behavior. AudioSwitch logging is disabled by default.<br><br>var [loggingEnabled](index.md#com.twilio.audioswitch/AudioSwitch/loggingEnabled/#/PointingToDeclaration/): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)   <br>
| [selectedAudioDevice](index.md#com.twilio.audioswitch/AudioSwitch/selectedAudioDevice/#/PointingToDeclaration/)|  [androidJvm] <br><br>Retrieves the selected [AudioDevice](../-audio-device/index.md) from [AudioSwitch.selectDevice](select-device.md).<br><br>val [selectedAudioDevice](index.md#com.twilio.audioswitch/AudioSwitch/selectedAudioDevice/#/PointingToDeclaration/): [AudioDevice](../-audio-device/index.md)?   <br>

