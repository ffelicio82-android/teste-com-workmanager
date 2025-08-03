package br.com.nukes.testeworkmanager.workers.configuration

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.InitialWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PipelineManager: KoinComponent {

    private val workManager: WorkManager by inject()

    fun initialize() {
        val initialWorker = OneTimeWorkRequestBuilder<InitialWorker>()
            .addTag(BaseWorker.Companion.DEFAULT_TAG)
            .addTag(InitialWorker.Companion.TAG)
            .build()

        workManager.enqueue(initialWorker)
    }
}