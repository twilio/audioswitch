package com.twilio.audioswitch.bluetooth

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.twilio.audioswitch.android.LogWrapper
import java.util.concurrent.TimeoutException

private const val TAG = "BluetoothScoManager"
private const val TIMEOUT = 5000L

internal class BluetoothScoJob(private val logger: LogWrapper, private val scoAction: () -> Unit) {

    private var bluetoothScoHandler: Handler = Handler(Looper.getMainLooper())
    private var bluetoothScoRunnable: BluetoothScoRunnable? = null

    fun executeBluetoothScoJob() {
        if (bluetoothScoRunnable == null) {
            bluetoothScoRunnable = BluetoothScoRunnable(scoAction)
            bluetoothScoHandler.post(bluetoothScoRunnable)
            logger.d(TAG, "Scheduled bluetooth sco job")
        }
    }

    fun cancelBluetoothScoJob() {
        bluetoothScoRunnable?.let {
            bluetoothScoHandler.removeCallbacks(it)
            bluetoothScoRunnable = null
            logger.d(TAG, "Canceled bluetooth sco job")
        }
    }

    inner class BluetoothScoRunnable(private val scoAction: () -> Unit) : Runnable {

        private val startTime = SystemClock.elapsedRealtime()
        private var elapsedTime = 0L

        override fun run() {
            if (elapsedTime < TIMEOUT) {
                logger.d(TAG, "Invoking bluetooth sco action")
                scoAction.invoke()
                bluetoothScoHandler.postDelayed(this, 500)
                elapsedTime = SystemClock.elapsedRealtime() - startTime
            } else {
                logger.e(TAG, "Bluetooth sco job timed out", TimeoutException())
                cancelBluetoothScoJob()
            }
        }
    }
}
