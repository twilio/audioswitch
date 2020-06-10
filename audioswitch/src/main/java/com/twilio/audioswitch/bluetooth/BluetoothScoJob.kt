package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "BluetoothScoManager"

internal class BluetoothScoJob(private val logger: LogWrapper) {

    private var activateBluetoothHandler: Handler = Handler(Looper.getMainLooper())
    private var activateBluetoothRunnable: Runnable? = null

    fun executeBluetoothScoJob(scoAction: () -> Unit) {
        activateBluetoothRunnable = BluetoothScoRunnable(scoAction)
        activateBluetoothHandler.post(activateBluetoothRunnable)
        logger.d(TAG, "Scheduled bluetooth sco job")
    }

    fun cancelBluetoothScoJob() {
        activateBluetoothHandler.removeCallbacks(activateBluetoothRunnable)
        logger.d(TAG, "Canceled bluetooth sco job")
    }

    inner class BluetoothScoRunnable(private val scoAction: () -> Unit) : Runnable {
        override fun run() {
            logger.d(TAG, "Invoking bluetooth sco action")
            scoAction.invoke()
            activateBluetoothHandler.postDelayed(this, 500)
        }
    }
}
