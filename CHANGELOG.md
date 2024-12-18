# Changelog
### 1.2.1 (In progress)

Enhancements

- AudioDeviceChangeListener is now optional parameter when calling `AudioSwitch.start(listener: AudioDeviceChangeListener? = null)`
- Added `AudioSwitch.setAudioDeviceChangeListener(listener: AudioDeviceChangeListener?)`

### 1.2.0 (June 3, 2024)

Enhancements

- Updated gradle version to 8.4
- Updated gradle plugin to 8.3.1
- BluetoothHeadsetConnectionListener now can be added to AudioSwitch to notify when bluetooth device has connected or failed to connect.
- BLUETOOTH_CONNECT and/or BLUETOOTH permission have been removed and are optional now. For bluetooth support, permission have to be added to application using
AudioSwitch library. If not provided bluetooth device will not appear in the list of available devices and no callbacks will be received for BluetoothHeadsetConnectionListener.

### 1.1.9 (July 13, 2023)

Enhancements

- Updated gradle version to 8.0.2
- Updated gradle plugin to 8.0.2

### 1.1.8 (Mar 17, 2023)

Bug Fixes

- Fixed issue where some Samsung Galaxy devices (S9, S21) would not route audio through USB headset when MODE_IN_COMMUNICATION is set.
- Fixed issue where IllegalStateException would be thrown when activating selected AudioDevice shortly after starting AudioSwitch.
- Fixed issue where after stopping AudioSwitch while having an active Bluetooth device would result in permanent audio focus gain.

### 1.1.7 (Feb 21, 2023)

Bug Fixes

- Bluetooth permissions now checks for the device version in case the target version is newer
- Documentation is now available again and integration tests now pass
- Fixed issue where reported Bluetooth device list could be incorrect upon AudioSwitch restart

### 1.1.5 (June 17, 2022)

Bug Fixes

- Fixed issue with lingering EnableBluetoothSCOJob object causing spurious AudioDeviceChangeListener calls after routing switch.

### 1.1.4 (January 4, 2022)

Enhancements

- Dokka dependency upgraded such that documents can be generated successfully again.
- Updated gradle version to 7.0.2
- Updated gradle plugin to 7.0.3

Bug Fixes

- Fixed issue with spurious `AudioDeviceChangedListener` invocations.
- Fixed issue where `InvalidStateException` would be triggered during `audioswitch.stop(..)` if bluetooth permissions were granted after 'AudioSwitch.start()`.

### 1.1.3 (November 5, 2021)

Enhancements

- Updated the library to support Android 12.
- Updated internal dependencies related to Android 12 support.
- Updated compile and target sdk to Android 12 (31).
- Updated gradle to version 4.2.1.
- Snapshots are now published to the Maven Central snapshots repository.

### 1.1.2 (February 24, 2021)

Enhancements

- Updated the library to use Android Gradle Plugin 4.1.1.
- Now published to MavenCentral.

### 1.1.1 (October 20, 2020)

Enhancements

- Added public KDoc documentation for each release. The latest documentation release can be found at https://twilio.github.io/audioswitch/latest

### 1.1.0 (October 8, 2020)

Enhancements

- Added a constructor parameter named `preferredDeviceList` to configure the order in which audio devices are automatically selected and activated when `selectedAudioDevice` is null.
```kotlin
val audioSwitch = AudioSwitch(application, preferredDeviceList = listOf(Speakerphone::class.java, BluetoothHeadset::class.java))
```
- Updated `compileSdkVersion` and `targetSdkVersion` to Android API version `30`.


### 1.0.1 (September 11, 2020)

Enhancements

- Upgraded Kotlin to `1.4.0`.
- Improved the Bluetooth headset connection and audio change reliability by registering the `BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED` and `BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED` intent actions instead of relying on `android.bluetooth.BluetoothDevice` and `android.media.AudioManager` intent actions.
- The context provided when constructing `AudioSwitch` can now take any context. Previously the `ApplicationContext` was required.

Bug Fixes

- Added the internal access modifier to the `SystemClockWrapper` class since it is not meant to be exposed publicly.

### 1.0.0 (August 17, 2020)

- Promotes 0.4.0 to the first stable release of this library.

### 0.4.0 (August 14, 2020)

Enhancements

- Added a constructor parameter to enable logging. This argument is disabled by default.

```kotlin
val audioSwitch = AudioSwitch(context, loggingEnabled = true)

audioSwitch.start { _, _ -> }
```

- Added another constructor parameter that allows developers to subscribe to system audio focus changes while the library is activated.

```kotlin
val audioSwitch = AudioSwitch(context, audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
    // Do something with audio focus change
})

audioSwitch.start { _, _ -> }
// Audio focus changes are received after activating
audioSwitch.activate()
```

### 0.3.0 (August 12, 2020)

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

### 0.2.1 (July 29, 2020)

Bug Fixes

- Fixed a bug where the audio focus wasn't being returned to the previous audio focus owner on pre Oreo devices.

### 0.2.0 (July 28, 2020)

Enhancements
- Added support for multiple connected bluetooth headsets.
  - The library will now accurately display the up to date active bluetooth headset within the `AudiodDeviceSelector` `availableAudioDevices` and `selectedAudioDevice` functions.
    - Other connected headsets are not stored by the library at this moment.
  - In the event of a failure to connecting audio to a bluetooth headset, the library will revert the selected audio device (this is usually the Earpiece on a phone).
  - If a user would like to switch between multiple Bluetooth headsets, then they need to switch the active bluetooth headset from the system Bluetooth settings.
    - The newly activated headset will be propagated to the `AudiodDeviceSelector` `availableAudioDevices` and `selectedAudioDevice` functions.

Bug Fixes

- Improved the accuracy of the `BluetoothHeadset` within the `availableAudioDevices` returned from the `AudioDeviceSelector` when multiple Bluetooth Headsets are connected.

### 0.1.5 (July 1, 2020)

Bug Fixes

- Disabled AAR minification to fix Android Studio issues such as getting stuck in code analysis and not being able to find declarations of AudioSwitch code.

### 0.1.4 (June 15, 2020)

Enhancements
- AAR minification is now enabled for release artifacts.

Bug Fixes

- Fixed a bug where the audio output doesn't automatically route to a newly connected bluetooth headset.
- Fixed a bug where the selected audio device doesn't get routed back to the default audio device when an error occurs when attempting to connect to a headset.

### 0.1.3 (May 27, 2020)

Bug Fixes

- Fixed crash by adding a default bluetooth device name.

### 0.1.2 (May 22, 2020)

Enhancements

- Added the library source to release artifacts. The sources will now be available when jumping to a library class definition in Android Studio.

Bug Fixes

- Added a fix for certain valid bluetooth device classes not being considered as headset devices as reported in [issue #16](https://github.com/twilio/audioswitch/issues/16).

### 0.1.1 (May 19, 2020)

- Fixes bug that did not correctly abandon audio request after deactivate

### 0.1.0 (April 28, 2020)

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

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.twilio/audioswitch/badge.svg) ](https://maven-badges.herokuapp.com/maven-central/com.twilio/audioswitch)

Ensure that you have `mavenCentral` listed in your project's buildscript repositories section:
```groovy
buildscript {
    repositories {
        mavenCentral()
        // ...
    }
}
```

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
