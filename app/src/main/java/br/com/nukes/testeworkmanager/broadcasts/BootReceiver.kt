package br.com.nukes.testeworkmanager.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.workers.configuration.WorkerScheduler

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> WorkerScheduler.scheduleWorkerOrchestrator(Constants.ZERO)
        }
    }
}