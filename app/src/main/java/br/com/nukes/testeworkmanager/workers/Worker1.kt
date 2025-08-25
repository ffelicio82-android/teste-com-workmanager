package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.workers.WorkerResult.Success
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
            Success()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun nextWorker() {
        workManager.enqueue(Worker2.configureRequest())
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

        fun configureRequest(): OneTimeWorkRequest  {
            return OneTimeWorkRequest.Builder(Worker1::class.java)
                .addTag(TAG)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}