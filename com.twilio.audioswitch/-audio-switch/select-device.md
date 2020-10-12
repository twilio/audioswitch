//[audioswitch](../../index.md)/[com.twilio.audioswitch](../index.md)/[AudioSwitch](index.md)/[selectDevice](select-device.md)



# selectDevice  
[androidJvm]  
Brief description  




Selects the desired [AudioDevice](../-audio-device/index.md). If the provided [AudioDevice](../-audio-device/index.md) is not available, no changes are made. If the provided device is null, one is chosen based on the specified preferred device list or the following default list:



Bluetooth, [WiredHeadset](../-audio-device/-wired-headset/index.md), [Earpiece](../-audio-device/-earpiece/index.md), [Speakerphone](../-audio-device/-speakerphone/index.md).



  
Content  
fun [selectDevice](select-device.md)(audioDevice: [AudioDevice](../-audio-device/index.md)?)  



