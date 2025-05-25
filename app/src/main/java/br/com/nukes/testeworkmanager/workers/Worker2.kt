package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class Worker2(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        // Simulate some work
        Log.d("WorkerUtils-Worker2", "Doing some work in Worker2 - time = ${System.currentTimeMillis()}")
        return Result.success()
    }
}