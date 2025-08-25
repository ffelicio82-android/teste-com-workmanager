package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.workers.WorkerResult.Failure
import br.com.nukes.testeworkmanager.workers.WorkerResult.Retry
import br.com.nukes.testeworkmanager.workers.WorkerResult.Success
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InitialWorker(
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
            when(e) {
                is SecurityException -> {
                    Log.e(TAG, "Security exception encountered, retrying...")
                    Failure()
                }
                else -> {
                    Log.e(TAG, "An unexpected error occurred: ${e.message}")
                    Retry()
                }
            }
        }
    }

    override suspend fun nextWorker() {
        workManager.enqueue(Worker1.configureRequest())
    }

    companion object {
        const val TAG = "initial_worker"

        fun configureRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<InitialWorker>()
                .addTag(DEFAULT_TAG)
                .addTag(TAG)
                .build()
        }
    }
}