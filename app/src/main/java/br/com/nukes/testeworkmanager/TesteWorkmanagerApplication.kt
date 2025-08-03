package br.com.nukes.testeworkmanager

import android.app.Application
import br.com.nukes.testeworkmanager.di.modules
import br.com.nukes.testeworkmanager.utils.Constants.ZERO
import br.com.nukes.testeworkmanager.workers.configuration.WorkerScheduler.scheduleWorkerOrchestrator
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class TesteWorkmanagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TesteWorkmanagerApplication)
            workManagerFactory()
            modules(modules)
        }

        scheduleWorkerOrchestrator(ZERO)
    }
}