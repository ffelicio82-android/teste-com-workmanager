package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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
            WorkerResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error executing work: ${e.message}", e)
            WorkerResult.Retry()
        }
    }

    override fun nextWorker() {
        val request = OneTimeWorkRequest.Builder(Worker3::class.java)
            .addTag(Worker3.TAG)
            .addTag(DEFAULT_TAG)
            .build()
        workManager.enqueue(request)
    }

    companion object Companion {
        const val TAG = "worker_2"
    }
}