---
title: AudioDevice -
---
//[audioswitch](../../index.md)/[com.twilio.audioswitch](../index.md)/[AudioDevice](index.md)



# AudioDevice  
 [androidJvm] 

This class represents a single audio device that has been retrieved by the [AudioSwitch](../-audio-switch/index.md).

sealed class [AudioDevice](index.md)   


## Types  
  
|  Name|  Summary| 
|---|---|
| [BluetoothHeadset](-bluetooth-headset/index.md)| [androidJvm]  <br>Brief description  <br><br><br>An [AudioDevice](index.md) representing a Bluetooth Headset.<br><br>  <br>Content  <br>data class [BluetoothHeadset](-bluetooth-headset/index.md) : [AudioDevice](index.md)  <br><br><br>
| [Earpiece](-earpiece/index.md)| [androidJvm]  <br>Brief description  <br><br><br>An [AudioDevice](index.md) representing the Earpiece.<br><br>  <br>Content  <br>data class [Earpiece](-earpiece/index.md) : [AudioDevice](index.md)  <br><br><br>
| [Speakerphone](-speakerphone/index.md)| [androidJvm]  <br>Brief description  <br><br><br>An [AudioDevice](index.md) representing the Speakerphone.<br><br>  <br>Content  <br>data class [Speakerphone](-speakerphone/index.md) : [AudioDevice](index.md)  <br><br><br>
| [WiredHeadset](-wired-headset/index.md)| [androidJvm]  <br>Brief description  <br><br><br>An [AudioDevice](index.md) representing a Wired Headset.<br><br>  <br>Content  <br>data class [WiredHeadset](-wired-headset/index.md) : [AudioDevice](index.md)  <br><br><br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| [equals](../-audio-switch/-companion/index.md#kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open operator override fun [equals](../-audio-switch/-companion/index.md#kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| [hashCode](../-audio-switch/-companion/index.md#kotlin/Any/hashCode/#/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open override fun [hashCode](../-audio-switch/-companion/index.md#kotlin/Any/hashCode/#/PointingToDeclaration/)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| [toString](../-audio-switch/-companion/index.md#kotlin/Any/toString/#/PointingToDeclaration/)| [androidJvm]  <br>Content  <br>open override fun [toString](../-audio-switch/-companion/index.md#kotlin/Any/toString/#/PointingToDeclaration/)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| [name](index.md#com.twilio.audioswitch/AudioDevice/name/#/PointingToDeclaration/)|  [androidJvm] <br><br>The friendly name of the device.<br><br>abstract val [name](index.md#com.twilio.audioswitch/AudioDevice/name/#/PointingToDeclaration/): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)   <br>


## Inheritors  
  
|  Name| 
|---|
| [AudioDevice](-bluetooth-headset/index.md)
| [AudioDevice](-wired-headset/index.md)
| [AudioDevice](-earpiece/index.md)
| [AudioDevice](-speakerphone/index.md)

