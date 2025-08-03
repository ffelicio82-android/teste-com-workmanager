package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent

class SendDataWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params), KoinComponent {

    override val key: String = TAG

    override val stopExecutionByKey: Boolean = true

    override suspend fun executeWork(): WorkerResult {
        return WorkerResult.Success
    }

    companion object {
        const val TAG = "send_data_worker"
    }
}