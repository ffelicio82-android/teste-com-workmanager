package br.com.nukes.testeworkmanager.workers.appManagement

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineStep
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import br.com.nukes.testeworkmanager.workers.dataflow.DownloadWorker
import br.com.nukes.testeworkmanager.workers.system.InstallBuildWorker
import br.com.nukes.testeworkmanager.workers.system.ProcessBuildWorker
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ProcessAppsWorker(
    context: Context,
    params: WorkerParameters,
    private val getAllAppsUseCase: GetAllAppsUseCase,
    pipelineController: PipelineController
) : BaseWorker(context, params, pipelineController), KoinComponent {
    private val workManager: WorkManager by inject()

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }

    override val key: String = "${TAG}_$batchId"

    override val step = PipelineStep.PROCESS_APPS

    override suspend fun executeWork(): WorkerResult {
        try {
            val apps = getAllAppsUseCase().getOrElse { emptyList() }

            if (apps.isEmpty()) {
                return WorkerResult.Success(
                    workDataOf(
                        "next" to InstallBuildWorker.TAG,
                        BATCH_ID to batchId
                    )
                )
            }

            // Pega o próximo app
            val app = apps.first()

            // Marcar como baixando

            return WorkerResult.Success(
                workDataOf(
                    "next" to TAG,
                    "action" to app.action,
                    "packageName" to app.packageName,
                    BATCH_ID to batchId,
                    DATA to Json.encodeToString(app)
                )
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun nextWorker(data: Data?) {
        val next = data?.getString("next")

        if (next == InstallBuildWorker.TAG) {
            workManager.enqueue(ProcessBuildWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId)))
            return
        }

        val action = data?.getString("action")
        val packageName = data?.getString("packageName")

        when (action?.uppercase()) {
            INSTALL_FLAG -> {
                workManager.enqueue(
                    DownloadWorker.configureRequest(
                        batchId,
                        data,
                        packageName?.trim()?.replace(".", "_")
                    )
                )
            }
            UNINSTALL_FLAG -> {
                workManager.enqueue(
                    UninstallAppWorker.configureRequest(
                        batchId,
                        data,
                        packageName?.trim()?.replace(".", "_")
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "process_apps_worker"
        const val INSTALL_FLAG = "I"
        const val UNINSTALL_FLAG = "D"

        fun configureRequest(batchId: String?, input: Data? = null): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<ProcessAppsWorker>()
                .addTag(TAG)
                .addTag(DEFAULT_TAG)

            batchId?.let { request.addTag("batch_$it") }
            input?.let { data -> request.setInputData(data) }

            return request.build()
        }
    }
}