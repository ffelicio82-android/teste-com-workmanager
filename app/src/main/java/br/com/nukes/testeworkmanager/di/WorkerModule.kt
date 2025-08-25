package br.com.nukes.testeworkmanager.di

import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.DownloadWorker
import br.com.nukes.testeworkmanager.workers.FinalizationProcessAppsWorker
import br.com.nukes.testeworkmanager.workers.InitialWorker
import br.com.nukes.testeworkmanager.workers.InstallAppWorker
import br.com.nukes.testeworkmanager.workers.InstallBuildWorker
import br.com.nukes.testeworkmanager.workers.SendNotificationWorker
import br.com.nukes.testeworkmanager.workers.UninstallAppWorker
import br.com.nukes.testeworkmanager.workers.SendRequestDataWorker
import br.com.nukes.testeworkmanager.workers.Worker1
import br.com.nukes.testeworkmanager.workers.Worker2
import br.com.nukes.testeworkmanager.workers.configuration.WorkerOrchestrator
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    single<WorkManager> { WorkManager.getInstance(get()) }

    worker { WorkerOrchestrator(get(), get(), get()) }
    worker { InitialWorker(get(), get()) }
    worker { Worker1(get(), get()) }
    worker { Worker2(get(), get()) }
    worker { SendRequestDataWorker(get(), get(), get(), get(), get()) }
    worker { DownloadWorker(get(), get(), get()) }
    worker { InstallAppWorker(get(), get()) }
    worker { UninstallAppWorker(get(), get()) }
    worker { FinalizationProcessAppsWorker(get(), get(), get(), get(), get()) }
    worker { InstallBuildWorker(get(), get(), get()) }
    worker { SendNotificationWorker(get(), get()) }
}