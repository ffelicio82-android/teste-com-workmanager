package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit.MILLISECONDS

object WorkerScheduler {
    fun scheduleWorkerOrchestrator(
        context: Context,
        delayMillis: Long = 0L,
        firstExecution: Boolean = false
    ) {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(WorkerOrchestrator.TAG).get()

        // Se não houver nenhum trabalho pendente, não agenda novamente
        if (workInfos.none { it.state == WorkInfo.State.ENQUEUED }.not()) {
            return
        }

        val input = Data.Builder().putBoolean("first_execution", firstExecution).build()

        val request = OneTimeWorkRequestBuilder<WorkerOrchestrator>()
            .setInitialDelay(delayMillis, MILLISECONDS)
            .setInputData(input)
            .addTag(WorkerOrchestrator.TAG)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = WorkerOrchestrator.TAG,
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
            request = request
        )
    }
}