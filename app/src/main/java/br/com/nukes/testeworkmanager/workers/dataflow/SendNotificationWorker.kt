package br.com.nukes.testeworkmanager.workers.dataflow

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import org.koin.core.component.KoinComponent

class SendNotificationWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params), KoinComponent {

    private val batchId by lazy { inputData.getString("batchId") ?: "no_batch" }

    override val key: String = "${TAG}_$batchId"

    override val stopExecutionByKey: Boolean = true

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}", "Executing send notification work in batch $batchId")
        return WorkerResult.Success()
    }

    companion object {
        const val TAG = "send_notification_worker"

        fun configureRequest(batchId: String?, input: Data? = null): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<SendNotificationWorker>()
                .addTag(TAG)
                .addTag(DEFAULT_TAG)

            batchId?.let { request.addTag("batch_$it") }
            input?.let { data -> request.setInputData(data) }

            return request.build()
        }
    }
}