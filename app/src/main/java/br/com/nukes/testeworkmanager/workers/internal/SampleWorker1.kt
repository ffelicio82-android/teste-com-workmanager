package br.com.nukes.testeworkmanager.workers.internal

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import br.com.nukes.testeworkmanager.data.SharedPreferencesHelper
import br.com.nukes.testeworkmanager.workers.internal.WorkerUtils.SAMPLE_WORKER_1
import java.util.concurrent.TimeUnit

class SampleWorker1(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val sharedPreferencesHelper : SharedPreferencesHelper by lazy { SharedPreferencesHelper(applicationContext) }
    private val workManager by lazy { WorkManager.getInstance(applicationContext) }

    override suspend fun doWork(): Result {
        // chama o WorkerUtils para executar os Workers em sequência
        WorkerUtils.startSequentialWorkers(applicationContext, 1L).await()

        // Pega as informações do SharedPreferences
        val interval = getUpdatedInterval()

        // Reagenda este Worker global após o intervalo definido
        scheduleNextOrchestratorRun(interval)

        Log.d("WorkerUtils-Sample1", "Agendando próxima execução em $interval minutos")

        return Result.success()
    }

    private fun getUpdatedInterval(): Long {
        val interval = sharedPreferencesHelper.getInterval().coerceAtLeast(15)
        return (interval..18).random()
    }

    private fun scheduleNextOrchestratorRun(intervalMinutes: Long) {
        val nextRequest = PeriodicWorkRequestBuilder<SampleWorker1>(intervalMinutes, TimeUnit.MINUTES)
            .addTag(SAMPLE_WORKER_1)
            .build()

        workManager.apply {
            // Cancela execuções pendentes e agenda uma nova
            enqueueUniquePeriodicWork(SAMPLE_WORKER_1, ExistingPeriodicWorkPolicy.UPDATE, nextRequest)
        }
    }
}