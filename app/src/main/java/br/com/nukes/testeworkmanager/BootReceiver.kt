package br.com.nukes.testeworkmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.nukes.testeworkmanager.workers.internal.WorkerUtils

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // WorkerUtils.startSampleWorker1(context)
                WorkerUtils.startSampleWorker2(context)
                // WorkerUtils.startSampleWorker3(context)
            }
        }
    }
}