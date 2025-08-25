package br.com.nukes.testeworkmanager.workers.configuration

import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.InitialWorker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PipelineManager: KoinComponent {

    private val workManager: WorkManager by inject()

    fun initialize() {
        workManager.enqueue(InitialWorker.configureRequest())
    }
}