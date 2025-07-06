package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class SendRequestDataWorker(
    context: Context,
    params: WorkerParameters,
) : BaseWorker(context, params) {
    override val key: String = TAG

    override fun executeWork(): WorkerResult {
        return try {
            Log.i(TAG, "Executing work ${System.currentTimeMillis()}")
            WorkerResult.Success
        } catch (e: Exception) {
            Log.e(ProcessAppsWorker.Companion.TAG, "Error executing work: ${e.message}", e)
            WorkerResult.Retry
        }
    }

    override fun nextWorker() {
        val request = OneTimeWorkRequest.Builder(ProcessAppsWorker::class.java)
            .addTag(ProcessAppsWorker.TAG)
            .addTag(DEFAULT_TAG)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    companion object {
        const val TAG = "send_request_data_worker"
    }
}