package com.twilio.audioswitch.android

import android.os.Build
import com.twilio.audioswitch.BuildConfig

internal class BuildWrapper {

    val version = Build.VERSION.SDK_INT

    val buildType = BuildConfig.BUILD_TYPE
}
