package br.com.nukes.testeworkmanager.workers.internal

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.Worker1
import br.com.nukes.testeworkmanager.workers.Worker2
import br.com.nukes.testeworkmanager.workers.Worker3
import java.util.concurrent.TimeUnit

object WorkerUtils {
    const val SAMPLE_WORKER_1 = "SampleWorker1"
    const val SAMPLE_WORKER_2 = "SampleWorker2"
    const val SAMPLE_WORKER_3 = "SampleWorker3"

    fun startSampleWorker1(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(SAMPLE_WORKER_1).get()

        if (workInfos.none { it.state == WorkInfo.State.ENQUEUED }) {
            val initialRequest = PeriodicWorkRequestBuilder<SampleWorker1>(15, TimeUnit.MINUTES)
                .addTag(SAMPLE_WORKER_1)
                .build()

            workManager.enqueueUniquePeriodicWork(
                SAMPLE_WORKER_1,
                ExistingPeriodicWorkPolicy.KEEP,
                initialRequest
            )
        }
    }

    fun startSampleWorker2(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(SAMPLE_WORKER_2).get()

        if (workInfos.none { it.state == WorkInfo.State.ENQUEUED }) {
            val initialRequest = OneTimeWorkRequestBuilder<SampleWorker2>()
                .addTag(SAMPLE_WORKER_2)
                .build()

            workManager.enqueueUniqueWork(SAMPLE_WORKER_2, ExistingWorkPolicy.KEEP, initialRequest)
        }
    }

    fun startSampleWorker3(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(SAMPLE_WORKER_3).get()

        if (workInfos.none { it.state == WorkInfo.State.ENQUEUED }) {
            val initialRequest = OneTimeWorkRequestBuilder<SampleWorker3>()
                .addTag(SAMPLE_WORKER_3)
                .build()

            workManager.enqueueUniqueWork(SAMPLE_WORKER_3, ExistingWorkPolicy.KEEP, initialRequest)
        }
    }

    fun startSequentialWorkers(context: Context, retryDelay: Long) : Operation {
        val workManager = WorkManager.getInstance(context)

        val worker1 = OneTimeWorkRequestBuilder<Worker1>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, retryDelay, TimeUnit.SECONDS)
            .build()

        val worker2 = OneTimeWorkRequestBuilder<Worker2>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, retryDelay, TimeUnit.SECONDS)
            .build()

        val worker3 = OneTimeWorkRequestBuilder<Worker3>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, retryDelay, TimeUnit.SECONDS)
            .build()

        // Encadeamento garantido com unicidade
        return workManager
            .beginWith(worker1)
            .then(worker2)
            .then(worker3)
            .enqueue()
    }
}