package br.com.nukes.testeworkmanager

import android.app.Application
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.workers.WorkerScheduler

class TesteWorkmanagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WorkerScheduler.scheduleWorkerOrchestrator(
            context = this,
            delayMillis = Constants.ZERO,
            firstExecution = true
        )
    }
}