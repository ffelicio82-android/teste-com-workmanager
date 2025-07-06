package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters

class SendNotifyWorker(
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

    companion object {
        const val TAG = "send_notify_worker"
    }
}