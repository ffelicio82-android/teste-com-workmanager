package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class Worker1(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Simulate some work
        Log.d("WorkerUtils-Worker1", "Doing some work in Worker1 - time = ${System.currentTimeMillis()}")
        return Result.success()
    }
}