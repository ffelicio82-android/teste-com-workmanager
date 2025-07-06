package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.utils.ConfigurationsHelper

class WorkerOrchestrator(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val configurationsHelper : ConfigurationsHelper by lazy {
        ConfigurationsHelper(context)
    }

    override fun doWork(): Result {
        Log.i(TAG, "Executing work ${System.currentTimeMillis()}")

        // aqui registra as configurações de tentativas fixas de execução dos workers
        PipelineManager.registerFixedWorkers(applicationContext)

        // Inicializa o pipeline de execução dos workers
        PipelineManager.initialize(applicationContext)

        val interval = configurationsHelper.fetchInterval()

        WorkerScheduler.scheduleWorkerOrchestrator(
            context = applicationContext,
            delayMillis = interval,
            firstExecution = false
        )

        return Result.success()
    }

    companion object {
        const val TAG = "worker_orchestrator"
    }
}