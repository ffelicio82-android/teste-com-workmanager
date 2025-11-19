package br.com.nukes.testeworkmanager.di

import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.workers.dataflow.DownloadWorker
import br.com.nukes.testeworkmanager.workers.dataflow.InitialWorker
import br.com.nukes.testeworkmanager.workers.appManagement.InstallAppWorker
import br.com.nukes.testeworkmanager.workers.system.InstallBuildWorker
import br.com.nukes.testeworkmanager.workers.dataflow.SendNotificationWorker
import br.com.nukes.testeworkmanager.workers.appManagement.UninstallAppWorker
import br.com.nukes.testeworkmanager.workers.dataflow.SendRequestDataWorker
import br.com.nukes.testeworkmanager.workers.appManagement.FetchInstalledAppsWorker
import br.com.nukes.testeworkmanager.workers.appManagement.FetchAppUsageReportWorker
import br.com.nukes.testeworkmanager.workers.appManagement.ProcessAppsWorker
import br.com.nukes.testeworkmanager.workers.configuration.WorkerOrchestrator
import br.com.nukes.testeworkmanager.workers.system.ProcessBuildWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    single<WorkManager> { WorkManager.getInstance(get()) }

    worker { WorkerOrchestrator(get(), get(), get()) }
    worker { InitialWorker(get(), get()) }
    worker { FetchInstalledAppsWorker(get(), get(), get()) }
    worker { FetchAppUsageReportWorker(get(), get(), get()) }
    worker { SendRequestDataWorker(get(), get(), get(), get(), get()) }
    worker { ProcessAppsWorker(get(), get(), get()) }
    worker { DownloadWorker(get(), get(), get()) }
    worker { InstallAppWorker(get(), get(), get()) }
    worker { UninstallAppWorker(get(), get(), get()) }
    worker { ProcessBuildWorker(get(), get(), get()) }
    worker { InstallBuildWorker(get(), get(), get()) }
    worker { SendNotificationWorker(get(), get()) }
}