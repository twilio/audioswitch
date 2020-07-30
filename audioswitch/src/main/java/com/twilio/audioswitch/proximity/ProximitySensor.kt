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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val proximityWakeLock =
                        powerManager.newWakeLock(
                                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "$TAG:wakelock")
                        ProximitySensor(logger, proximityWakeLock)
                    } else {
                    logger.d(TAG, "Proximity sensor is not available on API version < " +
                            "${Build.VERSION_CODES.LOLLIPOP}")
                    null
                }
            }
        }
    }

    fun activate() {
        proximityWakeLock.acquire()
        logger.d(TAG, "Acquired proximity sensor wake lock")
    }

    fun deactivate() {
        proximityWakeLock.release()
        logger.d(TAG, "Released proximity sensor wake lock")
    }
}
