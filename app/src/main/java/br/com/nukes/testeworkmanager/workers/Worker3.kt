package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class Worker3(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    // override var runAttemptCount: Int = 0

    override fun doWork(): Result {
        // Simulate some work
        Log.d("WorkerUtils-Worker3", "Doing some work in Worker3 - time = ${System.currentTimeMillis()}\n")
        return Result.success()
    }
}