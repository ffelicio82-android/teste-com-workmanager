package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.workers.WorkerResult.Retry
import br.com.nukes.testeworkmanager.workers.WorkerResult.Success
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Worker2(
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
            Log.e(TAG, "Error executing work: ${e.message}", e)
            Retry()
        }
    }

    override suspend fun nextWorker() {
        workManager.enqueue(SendRequestDataWorker.configureRequest())
    }

    companion object {
        const val TAG = "worker_2"

        fun configureRequest(): OneTimeWorkRequest  {
            return OneTimeWorkRequest.Builder(Worker2::class.java)
                .addTag(TAG)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}