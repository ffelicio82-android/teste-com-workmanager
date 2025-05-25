package br.com.nukes.testeworkmanager.workers.internal

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import br.com.nukes.testeworkmanager.data.SharedPreferencesHelper
import br.com.nukes.testeworkmanager.workers.Worker1
import br.com.nukes.testeworkmanager.workers.Worker2
import br.com.nukes.testeworkmanager.workers.Worker3
import br.com.nukes.testeworkmanager.workers.internal.WorkerUtils.SAMPLE_WORKER_3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SampleWorker3(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    private val sharedPreferencesHelper : SharedPreferencesHelper by lazy { SharedPreferencesHelper(applicationContext) }
    private val workManager by lazy { WorkManager.getInstance(applicationContext) }

    override suspend fun doWork(): Result {
        val worker1 = OneTimeWorkRequestBuilder<Worker1>()
            .addTag("worker1")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1L, TimeUnit.SECONDS)
            .build()

        val worker2 = OneTimeWorkRequestBuilder<Worker2>()
            .addTag("worker2")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1L, TimeUnit.SECONDS)
            .build()

        val worker3 = OneTimeWorkRequestBuilder<Worker3>()
            .addTag("worker3")
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1L, TimeUnit.SECONDS)
            .build()

        // workers
        val workers = listOf(worker1, worker2, worker3)

        // Executa cada Worker sequencialmente, pulando falhas
        for (worker in workers) {
            try {
                workManager.enqueue(worker).await()
                val workInfo = withContext(Dispatchers.IO) {
                    workManager.getWorkInfoById(worker.id).get()
                }

                if (workInfo == null || workInfo.state == WorkInfo.State.FAILED) {
                    Log.w(TAG, "${worker.tags} falhou, pulando...")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao executar ${worker.tags}: ${e.message}")
            }
        }

        // Reagenda o próximo ciclo (mesmo com falhas)
        val intervalMinutes = getUpdatedInterval() // Pega do SharedPreferences
        Log.d(TAG, "Agendando próxima execução para $intervalMinutes minutos")
        scheduleNextOrchestratorRun(intervalMinutes)

        return Result.success()
    }

    private fun getUpdatedInterval(): Long {
        return (sharedPreferencesHelper.getInterval()..3).random()
    }

    private fun scheduleNextOrchestratorRun(intervalMinutes: Long) {
        val nextRequest = OneTimeWorkRequestBuilder<SampleWorker3>()
            .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
            .addTag(SAMPLE_WORKER_3)
            .build()

        workManager.apply {
            cancelAllWorkByTag(SAMPLE_WORKER_3)
            enqueueUniqueWork(SAMPLE_WORKER_3, ExistingWorkPolicy.KEEP, nextRequest)
        }
    }

    companion object {
        const val TAG = "WorkerUtils-SampleWorker3"
    }
}