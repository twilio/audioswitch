package com.twilio.audioswitch.proximity

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "ProximitySensor"

internal class ProximitySensor(
    private val logger: LogWrapper,
    private val proximityWakeLock: WakeLock
) {

    companion object {
        fun newInstance(context: Context, logger: LogWrapper): ProximitySensor? {
            return (context.applicationContext
                    .getSystemService(Context.POWER_SERVICE) as PowerManager?)?.let { powerManager ->
                val proximityWakeLock =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        powerManager.newWakeLock(
                                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "$TAG:wakelock")
                } else {
                    try {
                        powerManager.newWakeLock(
                                32, "$TAG:wakelock")
                    } catch (e: Exception) {
                        logger.e(TAG, "Could not get proximity wake lock")
                        null
                    }
                }
                proximityWakeLock?.let { ProximitySensor(logger, proximityWakeLock) }
            }
        }
    }

    fun activate() {
        if(!proximityWakeLock.isHeld) {
            proximityWakeLock.acquire()
        }
        logger.d(TAG, "Acquired proximity sensor wake lock")
    }

    fun deactivate() {
        if(proximityWakeLock.isHeld) {
            proximityWakeLock.release()
        }
        logger.d(TAG, "Released proximity sensor wake lock")
    }
}
