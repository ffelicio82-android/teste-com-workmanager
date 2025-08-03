package br.com.nukes.testeworkmanager.workers.configuration

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit.SECONDS

object WorkerScheduler: KoinComponent {

    private val workManager: WorkManager by inject()

    fun scheduleWorkerOrchestrator(seconds: Long = 0L) {
        val workInfos = workManager.getWorkInfosByTag(WorkerOrchestrator.TAG).get()

        if (workInfos.none { it.state == WorkInfo.State.ENQUEUED }.not()) {
            return
        }

        val request = OneTimeWorkRequestBuilder<WorkerOrchestrator>()
            .setInitialDelay(seconds, SECONDS)
            .addTag(WorkerOrchestrator.TAG)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = WorkerOrchestrator.TAG,
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
            request = request
        )
    }
}