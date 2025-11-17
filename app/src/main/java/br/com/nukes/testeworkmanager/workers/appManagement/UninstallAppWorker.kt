package br.com.nukes.testeworkmanager.workers.appManagement

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class UninstallAppWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString(Constants.DATA) ?: throw IllegalArgumentException("AppModel is required")
        Json.Default.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString("batchId") ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }

    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}}", "Executing uninstall app work ${appModel.packageName} in batch $batchId")

        delay(TimeUnit.MILLISECONDS.toSeconds(3L))

        // Uninstall the app
        return WorkerResult.Success(
            workDataOf(
                Constants.DATA to Json.Default.encodeToString(appModel),
                Constants.BATCH_ID to batchId
            )
        ).also {
                Log.i("Fernando-tag_${InstallAppWorker.Companion.TAG}", "Successfully uninstalled ${appModel.packageName} in batch $batchId")
            }
    }

    override suspend fun nextWorker(data: Data?) {
        Log.i("Fernando-tag_${TAG}}", "Executing uninstall nextWorker ${appModel.packageName} in batch $batchId")
        workManager.enqueue(FinalizationProcessAppsWorker.Companion.configureRequest(batchId, data, pkgSafe))
    }

    override suspend fun onAttemptsExhausted(data: Data?) {
        super.onAttemptsExhausted(data)

        // remove from DB
        Log.i("Fernando-tag_${TAG}}", "onAttemptsExhausted ${appModel.packageName} in batch $batchId")

         val json = Json.Default.encodeToString(appModel)
         val input = workDataOf(Constants.DATA to json, Constants.BATCH_ID to batchId)
         workManager.enqueue(FinalizationProcessAppsWorker.Companion.configureRequest(batchId, input, pkgSafe))
    }

    companion object {
        const val TAG = "uninstall_app_worker"

        fun configureRequest(batchId: String, input: Data, pkgSafe: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<UninstallAppWorker>()
                .setInputData(input)
                .addTag(TAG)
                .addTag("${TAG}_$pkgSafe")
                .addTag("batch_$batchId")
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}