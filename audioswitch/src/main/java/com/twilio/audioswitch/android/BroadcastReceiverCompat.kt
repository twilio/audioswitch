package com.twilio.audioswitch.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build

internal fun registerReceiverCompat(
    context: Context,
    broadcastReceiver: BroadcastReceiver,
    intentFilter: IntentFilter,
    exported: Boolean,
    broadcastPermission: String?,
) {
    if (Build.VERSION.SDK_INT >= 34 && context.applicationInfo.targetSdkVersion >= 34) {
        val flag = if (exported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        context.registerReceiver(broadcastReceiver, intentFilter, broadcastPermission, null, flag)
    } else {
        context.registerReceiver(broadcastReceiver, intentFilter, broadcastPermission, null)
    }
}
