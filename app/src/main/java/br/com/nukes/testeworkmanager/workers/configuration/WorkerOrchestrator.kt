package br.com.nukes.testeworkmanager.workers.configuration

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.usecases.FetchConfigurationsUseCase
import br.com.nukes.testeworkmanager.utils.Constants.SIXTY
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineManager

class WorkerOrchestrator(
    context: Context,
    params: WorkerParameters,
    private val fetchConfigurationsUseCase : FetchConfigurationsUseCase,
    private val pipelineController: PipelineController
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        pipelineController.start()

        Log.i(TAG, "Executing work ${System.currentTimeMillis()}")

        // Inicializa o pipeline de execução dos workers
        PipelineManager.initialize()

        fetchConfigurationsUseCase()
            .onSuccess { WorkerScheduler.scheduleWorkerOrchestrator(it.syncFrequency) }
            .onFailure { WorkerScheduler.scheduleWorkerOrchestrator(SIXTY) }

        return Result.success()
    }

    companion object {
        const val TAG = "worker_orchestrator"
    }
}