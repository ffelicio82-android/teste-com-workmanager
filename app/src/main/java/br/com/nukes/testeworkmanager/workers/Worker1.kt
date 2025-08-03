package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Worker1(
    context: Context,
    params: WorkerParameters,
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        return try {
            Log.i(TAG, "Executing work ${System.currentTimeMillis()}")
            WorkerResult.Success
        } catch (e: Exception) {
            throw e
        }
    }

    override fun nextWorker() {
        val request = OneTimeWorkRequest.Builder(Worker2::class.java)
            .addTag(Worker2.TAG)
            .addTag(DEFAULT_TAG)
            .build()
        workManager.enqueue(request)
    }

    override fun finishAllExecutions(callInRetry: Boolean) {
        super.finishAllExecutions(callInRetry)

        if (callInRetry) {
            Log.e(TAG, "Error executing work, call in retry process...")
        } else {
            Log.e(TAG, "Error executing work, finishing...")
        }
    }

    companion object Companion {
        const val TAG = "worker_1"
    }
}