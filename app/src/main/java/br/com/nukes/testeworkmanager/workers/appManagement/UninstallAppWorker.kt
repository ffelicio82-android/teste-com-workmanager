package br.com.nukes.testeworkmanager.workers.appManagement

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.usecases.DeleteByPackageNameUseCase
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineStep
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class UninstallAppWorker(
    context: Context,
    params: WorkerParameters,
    private val deleteByPackageNameUseCase: DeleteByPackageNameUseCase,
    private val pipelineController: PipelineController
) : BaseWorker(context, params, pipelineController), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString(Constants.DATA) ?: throw IllegalArgumentException("AppModel is required")
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString("batchId") ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }

    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override val step = PipelineStep.UNINSTALL_APP

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}}", "Executing uninstall app work ${appModel.packageName} in batch $batchId")

        delay(TimeUnit.MILLISECONDS.toSeconds(3L))

        // Uninstall the app
        return WorkerResult.Success(
            workDataOf(
                Constants.DATA to Json.encodeToString(appModel),
                BATCH_ID to batchId
            )
        ).also {
            Log.i("Fernando-tag_${InstallAppWorker.TAG}", "Successfully uninstalled ${appModel.packageName} in batch $batchId")
        }
    }

    override suspend fun nextWorker(data: Data?) {
        // remove from DB
        if (deleteByPackageNameUseCase(appModel.packageName).isSuccess) {
            Log.i("Fernando-tag_${TAG}}", "onAttemptsExhausted ${appModel.packageName} in batch $batchId")
            workManager.enqueue(ProcessAppsWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId)))
        }
    }

    override suspend fun onAttemptsExhausted(data: Data?) {
        super.onAttemptsExhausted(data)

        // remove from DB
        if (deleteByPackageNameUseCase(appModel.packageName).isSuccess) {
            Log.i("Fernando-tag_${TAG}}", "onAttemptsExhausted ${appModel.packageName} in batch $batchId")
            workManager.enqueue(ProcessAppsWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId)))
        }
    }

    companion object {
        const val TAG = "uninstall_app_worker"

        fun configureRequest(batchId: String?, input: Data?, pkgSafe: String?): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<UninstallAppWorker>()
                .addTag(TAG)
                .addTag(DEFAULT_TAG)

            batchId?.let { request.addTag("batch_$it") }
            input?.let { data -> request.setInputData(data) }
            pkgSafe?.let { request.addTag("${TAG}_$pkgSafe") }

            return request.build()
        }
    }
}