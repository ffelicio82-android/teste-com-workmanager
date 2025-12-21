package br.com.nukes.testeworkmanager.workers.dataflow

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineStep
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.appManagement.FetchInstalledAppsWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InitialWorker(
    context: Context,
    params: WorkerParameters,
    pipelineController: PipelineController
) : BaseWorker(context, params, pipelineController), KoinComponent {

    private val workManager: WorkManager by inject()

    override val step = PipelineStep.INITIAL

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        Log.i(TAG, "Executing work ${System.currentTimeMillis()}")
        return WorkerResult.Success()
    }

    override suspend fun nextWorker(data: Data?) {
        workManager.enqueue(FetchInstalledAppsWorker.configureRequest())
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