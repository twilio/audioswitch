package com.twilio.audioswitch.android

import android.os.AsyncTask

class AsyncTaskWrapper {

    fun execute(runnable: () -> Unit) =
            AsyncTask.execute(runnable)
}
