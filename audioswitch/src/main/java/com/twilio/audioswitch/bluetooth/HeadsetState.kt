package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.HeadsetState.State.Disconnected

private const val TAG = "HeadsetState"

internal class HeadsetState(private val logger: LogWrapper) {

    var state: State = Disconnected
        set(value) {
            if (field != value) {
                field = value
                logger?.d(TAG, "Headset state changed to $field")
            }
        }

    sealed class State {
        object Disconnected : State()
        object Connected : State()
        object Activated : State()
    }
}
