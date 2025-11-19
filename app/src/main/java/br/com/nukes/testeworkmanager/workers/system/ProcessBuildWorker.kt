package br.com.nukes.testeworkmanager.workers.system

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import br.com.nukes.testeworkmanager.workers.dataflow.DownloadWorker
import br.com.nukes.testeworkmanager.workers.dataflow.SendNotificationWorker
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProcessBuildWorker(
    context: Context,
    params: WorkerParameters,
    private val fetchByPackageNameUseCase: FetchByPackageNameUseCase
) : BaseWorker(context, params), KoinComponent {
    private val workManager: WorkManager by inject()

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }

    override val key: String = "${TAG}_$batchId"

    override suspend fun executeWork(): WorkerResult {
        return try {
            val build = fetchByPackageNameUseCase(InstallBuildWorker.BUILD).getOrThrow()

            build?.let { appModel ->
                val json = Json.encodeToString(appModel)
                val input = workDataOf(DATA to json, BATCH_ID to batchId, "packageName" to appModel.packageName)
                WorkerResult.Success(input)
            } ?: run {
                WorkerResult.Success()
            }
        } catch (_: Exception) {
            WorkerResult.Retry()
        }
    }

    override suspend fun nextWorker(data: Data?) {
        data?.let {
            val packageName = it.getString("packageName")
            workManager.enqueue(
                DownloadWorker.configureRequest(
                    batchId,
                    it,
                    packageName?.trim()?.replace(".", "_")
                )
            )
        } ?: run {
            workManager.enqueue(SendNotificationWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId)))
        }
    }

    companion object {
        const val TAG = "process_build_worker"

        fun configureRequest(batchId: String?, input: Data? = null): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<ProcessBuildWorker>()
                .addTag(TAG)
                .addTag(DEFAULT_TAG)

            batchId?.let {request.addTag("batch_$it") }
            input?.let { data -> request.setInputData(data) }

            return request.build()
        }
    }
}