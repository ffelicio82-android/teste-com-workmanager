package br.com.nukes.testeworkmanager.workers.internal

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import br.com.nukes.testeworkmanager.data.SharedPreferencesHelper
import br.com.nukes.testeworkmanager.workers.internal.WorkerUtils.SAMPLE_WORKER_2
import java.util.concurrent.TimeUnit

class SampleWorker2(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    private val sharedPreferencesHelper : SharedPreferencesHelper by lazy {
        SharedPreferencesHelper(applicationContext)
    }

    private val workManager by lazy {
        WorkManager.getInstance(applicationContext)
    }

    override suspend fun doWork(): Result {
        WorkerUtils.startSequentialWorkers(applicationContext, 1L)

        // Pega as informações do SharedPreferences
        val interval = getUpdatedInterval()

        // Reagenda este Worker global após o intervalo definido
        scheduleNextOrchestratorRun(interval)

        Log.d("WorkerUtils-Sample2", "Agendando próxima execução em $interval minutos")

        return Result.success()
    }

    private fun getUpdatedInterval(): Long {
        return (sharedPreferencesHelper.getInterval()..3).random()
    }

    private fun scheduleNextOrchestratorRun(intervalMinutes: Long) {
        val nextRequest = OneTimeWorkRequestBuilder<SampleWorker2>()
            .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
            .addTag(SAMPLE_WORKER_2) // Tag para evitar duplicação
            .build()

        // Cancela execuções pendentes e agenda uma nova
        workManager.apply {
            cancelAllWorkByTag(SAMPLE_WORKER_2)
            enqueueUniqueWork(SAMPLE_WORKER_2, ExistingWorkPolicy.REPLACE, nextRequest)
        }
    }
}