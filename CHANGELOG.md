# Changelog

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
