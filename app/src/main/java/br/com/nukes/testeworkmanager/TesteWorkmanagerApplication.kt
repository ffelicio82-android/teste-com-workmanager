package br.com.nukes.testeworkmanager

import android.app.Application
import br.com.nukes.testeworkmanager.workers.internal.WorkerUtils

class TesteWorkmanagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // WorkerUtils.startSampleWorker1(this)
        WorkerUtils.startSampleWorker2(this)
        // WorkerUtils.startSampleWorker3(this)
    }
}