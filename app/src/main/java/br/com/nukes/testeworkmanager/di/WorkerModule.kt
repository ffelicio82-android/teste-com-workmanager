package br.com.nukes.testeworkmanager.di

import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.DownloadWorker
import br.com.nukes.testeworkmanager.workers.FinalizationProcessAppsWorker
import br.com.nukes.testeworkmanager.workers.InstallAppWorker
import br.com.nukes.testeworkmanager.workers.SendDataWorker
import br.com.nukes.testeworkmanager.workers.UninstallAppWorker
import br.com.nukes.testeworkmanager.workers.Worker3
import br.com.nukes.testeworkmanager.workers.Worker4
import br.com.nukes.testeworkmanager.workers.configuration.WorkerOrchestrator
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    single<WorkManager> { WorkManager.getInstance(get()) }

    worker { WorkerOrchestrator(get(), get(), get()) }
    worker { Worker3(get(), get(), get(), get(), get()) }
    worker { FinalizationProcessAppsWorker(get(), get(), get(), get()) }
    worker { Worker4(get(), get(), get()) }
    worker { DownloadWorker(get(), get()) }
    worker { UninstallAppWorker(get(), get(), get()) }
    worker { InstallAppWorker(get(), get(), get()) }
    worker { SendDataWorker(get(), get()) }
}