# Changelog

### 0.4.0

Enhancements

- Added a constructor parameter to enable logging. This argument is disabled by default.

```kotlin
val audioSwitch = AudioSwitch(context, loggingEnabled = true)

audioSwitch.start { _, _ -> }
```

### 0.3.0

Enhancements

 - Changed the name of the `AudioDeviceSelector` class to `AudioSwitch`.
 - Added the [MODIFY_AUDIO_SETTINGS](https://developer.android.com/reference/android/Manifest.permission#MODIFY_AUDIO_SETTINGS) to the library manifest so it can be automatically consumed by applications.
 - Added `AudioSwitch.VERSION` constant so developers can access the version of AudioSwitch at runtime.
 - Added `AudioSwitch.loggingEnabled` property so developers can configure AudioSwitch logging behavior at runtime. By default, AudioSwitch logging is disabled. Reference the following snippet to enable AudioSwitch logging:

 ```kotlin
val audioSwitch = AudioSwitch(context)

audioSwitch.loggingEnabled = true

audioSwitch.start { _, _ -> }
```

### 0.2.1

Bug Fixes

- Fixed a bug where the audio focus wasn't being returned to the previous audio focus owner on pre Oreo devices.

### 0.2.0

Enhancements
- Added support for multiple connected bluetooth headsets.
  - The library will now accurately display the up to date active bluetooth headset within the `AudiodDeviceSelector` `availableAudioDevices` and `selectedAudioDevice` functions.
    - Other connected headsets are not stored by the library at this moment.
  - In the event of a failure to connecting audio to a bluetooth headset, the library will revert the selected audio device (this is usually the Earpiece on a phone).
  - If a user would like to switch between multiple Bluetooth headsets, then they need to switch the active bluetooth headset from the system Bluetooth settings.
    - The newly activated headset will be propagated to the `AudiodDeviceSelector` `availableAudioDevices` and `selectedAudioDevice` functions.

Bug Fixes

- Improved the accuracy of the `BluetoothHeadset` within the `availableAudioDevices` returned from the `AudioDeviceSelector` when multiple Bluetooth Headsets are connected.

### 0.1.5

Bug Fixes

- Disabled AAR minification to fix Android Studio issues such as getting stuck in code analysis and not being able to find declarations of AudioSwitch code.

### 0.1.4

Enhancements
- AAR minification is now enabled for release artifacts.

Bug Fixes

- Fixed a bug where the audio output doesn't automatically route to a newly connected bluetooth headset.
- Fixed a bug where the selected audio device doesn't get routed back to the default audio device when an error occurs when attempting to connect to a headset.

### 0.1.3

Bug Fixes

- Fixed crash by adding a default bluetooth device name.

### 0.1.2

Enhancements

- Added the library source to release artifacts. The sources will now be available when jumping to a library class definition in Android Studio.

Bug Fixes

- Added a fix for certain valid bluetooth device classes not being considered as headset devices as reported in [issue #16](https://github.com/twilio/audioswitch/issues/16).

### 0.1.1

- Fixes bug that did not correctly abandon audio request after deactivate

### 0.1.0

This release marks the first iteration of the AudioSwitch library: an Android audio management library for real-time communication apps.

This initial release comes with the following features:

-  Manage [audio focus](https://developer.android.com/guide/topics/media-apps/audio-focus) for typical VoIP and Video conferencing use cases.
-  Manage audio input and output devices.
    -  Detect changes in available audio devices
    -  Enumerate audio devices
    -  Select an audio device

## Getting Started

To get started using this library, follow the steps below.

### Gradle Setup

[![Download](https://api.bintray.com/packages/twilio/releases/audioswitch/images/download.svg) ](https://bintray.com/twilio/releases/audioswitch/_latestVersion)

Add this line as a new Gradle dependency:
```groovy
implementation 'com.twilio:audioswitch:$version'
```

### AudioDeviceSelector Setup
Instantiate an instance of the [AudioDeviceSelector](audioswitch/src/main/java/com/twilio/audioswitch/selection/AudioDeviceSelector.kt) class, passing a reference to the application context.

```kotlin
val audioDeviceSelector = AudioDeviceSelector(applicationContext)
```

### Listen for Devices
To begin listening for live audio device changes, call the start function and pass a lambda that will receive [AudioDevices](audioswitch/src/main/java/com/twilio/audioswitch/selection/AudioDevice.kt) when they become available.

```kotlin
audioDeviceSelector.start { audioDevices, selectedDevice ->
    // TODO update UI with audio devices
}
```
You can also retrieve the available and selected audio devices manually at any time by calling the following properties:
```kotlin
val devices: List<AudioDevice> = audioDeviceSelector.availableAudioDevices
val selectedDevice: AudioDevice? = audioDeviceSelector.selectedAudioDevice
```
**Note:** Don't forget to stop listening for audio devices when no longer needed in order to prevent a memory leak.
```kotlin
audioDeviceSelector.stop()
```

### Select a Device
Before activating an AudioDevice, it needs to be selected first.
```kotlin
devices.find { it is AudioDevice.Speakerphone }?.let { audioDeviceSelector.selectDevice(it) }
```
If no device is selected, then the library will automatically select a device based on the following priority: `BluetoothHeadset -> WiredHeadset -> Earpiece -> Speakerphone`.

### Activate a Device
Activating a device acquires audio focus with [voice communication usage](https://developer.android.com/reference/android/media/AudioAttributes#USAGE_VOICE_COMMUNICATION) and begins routing audio input/output to the selected device.
```kotlin
audioDeviceSelector.activate()
```
Make sure to revert back to the prior audio state when it makes sense to do so in your app.
```kotlin
audioDeviceSelector.deactivate()
```
**Note:** The `stop()` function will call `deactivate()` before closing AudioDeviceSelector resources.
