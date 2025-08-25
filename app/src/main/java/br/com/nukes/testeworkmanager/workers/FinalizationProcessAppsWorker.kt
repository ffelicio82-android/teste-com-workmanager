package br.com.nukes.testeworkmanager.workers

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
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.workers.RetryReason.Database
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class FinalizationProcessAppsWorker(
    context: Context,
    params: WorkerParameters,
    private val getAllAppsUseCase: GetAllAppsUseCase,
    private val deleteByPackageNameUseCase: DeleteByPackageNameUseCase,
    private val fetchByPackageNameUseCase: FetchByPackageNameUseCase
) : BaseWorker(context, params) {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel? by lazy {
        val json = inputData.getString(DATA) ?: return@lazy null
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}", "Executing $TAG in batch $batchId")

        return appModel?.let { app ->
            deleteByPackageNameUseCase(app.packageName).fold(
                onSuccess = { _ ->
                    Log.i("Fernando-tag_${TAG}", "Deleted ${app.packageName} in batch $batchId")
                    WorkerResult.Success()
                },
                onFailure = { error ->
                    Log.e("Fernando-tag_${TAG}", "Error deleting package ${app.packageName} in batch $batchId", error)
                    WorkerResult.Retry(Database)
                }
            )
        } ?: WorkerResult.Success()
    }

    override suspend fun nextWorker() {
        val apps = getAllAppsUseCase().getOrElse { emptyList() }

        if (apps.isNotEmpty()) {
            return
        }

        val workerRequest = fetchByPackageNameUseCase("build").fold(
            onSuccess = { build ->
                when (build) {
                    null -> SendNotificationWorker.configureRequest(batchId)
                    else -> {
                        val json = Json.encodeToString(build)
                        val input = workDataOf(DATA to json, BATCH_ID to batchId)
                        DownloadWorker.configureRequest(batchId, input, build.packageName)
                    }
                }
            },
            onFailure = { _ -> SendNotificationWorker.configureRequest(batchId) }
        )

        workManager.enqueue(workerRequest)
    }

    companion object {
        const val TAG = "finalization_process_apps_worker"

        fun configureRequest(batchId: String, input: Data? = null, pkgSafe: String? = null): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<FinalizationProcessAppsWorker>()
                .setInputData(input ?: Data.EMPTY)
                .addTag(TAG)
                .addTag("batch_$batchId")
                .addTag(DEFAULT_TAG)

            if (pkgSafe != null) {
                request.addTag("${TAG}_$pkgSafe")
            }

            return request.build()
        }
    }
}