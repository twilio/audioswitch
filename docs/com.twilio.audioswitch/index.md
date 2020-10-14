---
title: com.twilio.audioswitch -
---
//[audioswitch](../index.md)/[com.twilio.audioswitch](index.md)



# Package com.twilio.audioswitch  


## Types  
  
|  Name|  Summary| 
|---|---|
| [AudioDevice](-audio-device/index.md)| [androidJvm]  <br>Brief description  <br><br><br>This class represents a single audio device that has been retrieved by the [AudioSwitch](-audio-switch/index.md).<br><br>  <br>Content  <br>sealed class [AudioDevice](-audio-device/index.md)  <br><br><br>
| [AudioDeviceChangeListener](index.md#com.twilio.audioswitch/AudioDeviceChangeListener///PointingToDeclaration/)| [androidJvm]  <br>Brief description  <br><br><br>Receives a list of the most recently available [AudioDevice](-audio-device/index.md)s. Also provides the currently selected [AudioDevice](-audio-device/index.md) from [AudioSwitch](-audio-switch/index.md).<br><br> **audioDevices** - The list of [AudioDevice](-audio-device/index.md)s or an empty list if none are available.<br> **selectedAudioDevice** - The currently selected device or null if no device has been selected.<br><br>  <br>Content  <br>typealias [AudioDeviceChangeListener](index.md#com.twilio.audioswitch/AudioDeviceChangeListener///PointingToDeclaration/) = ([List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[AudioDevice](-audio-device/index.md)>, [AudioDevice](-audio-device/index.md)?) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)  <br><br><br>
| [AudioSwitch](-audio-switch/index.md)| [androidJvm]  <br>Brief description  <br><br><br>This class enables developers to enumerate available audio devices and select which device audio should be routed to. It is strongly recommended that instances of this class are created and accessed from a single application thread. Accessing an instance from multiple threads may cause synchronization problems.<br><br>  <br>Content  <br>class [AudioSwitch](-audio-switch/index.md)  <br><br><br>

