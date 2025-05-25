package br.com.nukes.testeworkmanager

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log

class WorkManagerService: JobService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(WorkManagerService::class.simpleName, "Serviço criado e rodando silenciosamente.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(WorkManagerService::class.simpleName, "Serviço iniciado.")


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(WorkManagerService::class.simpleName, "Serviço destruído. Reiniciando...")

        val serviceIntent = Intent(this, WorkManagerService::class.java)
        startService(serviceIntent)
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        TODO("Not yet implemented")
    }
}