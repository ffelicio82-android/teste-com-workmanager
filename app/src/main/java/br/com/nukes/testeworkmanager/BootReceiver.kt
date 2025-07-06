package br.com.nukes.testeworkmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.workers.WorkerScheduler

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                WorkerScheduler.scheduleWorkerOrchestrator(
                    context,
                    delayMillis = Constants.ZERO,
                    firstExecution = true
                )
            }
        }
    }
}