package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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

        fun configureRequest(batchId: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SendNotificationWorker>()
                .setInputData(workDataOf("batchId" to batchId))
                .addTag(TAG)
                .addTag("batch_$batchId")
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}