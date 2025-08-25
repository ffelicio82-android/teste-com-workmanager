package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.usecases.DeleteByPackageNameUseCase
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InstallBuildWorker(
    context: Context,
    params: WorkerParameters,
    private val deleteByPackageNameUseCase: DeleteByPackageNameUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString(DATA) ?: throw IllegalArgumentException("AppModel is required")
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString("batchId") ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }

    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_$TAG}", "Executing install build work ${appModel.packageName} in batch $batchId")

        return WorkerResult.Success()
    }

    override suspend fun onBeforeNextWorker() {
        deleteByPackageNameUseCase(appModel.packageName).fold(
            onSuccess = {
                Log.i("Fernando-tag_$TAG}", "Deleted build ${appModel.packageName} from database in batch $batchId")
            },
            onFailure = { error ->
                Log.e("Fernando-tag_$TAG}", "Error deleting build ${appModel.packageName} from database in batch $batchId: ${error.message}", error)
            }
        )
    }

    override suspend fun nextWorker() {
        workManager.enqueue(SendNotificationWorker.configureRequest(batchId))
    }

    companion object {
        const val TAG = "install_build_worker"
        const val BUILD = "build"

        fun configureRequest(batchId: String, input: Data, pkgSafe: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<InstallBuildWorker>()
                .setInputData(input)
                .addTag(TAG)
                .addTag("${TAG}_$pkgSafe")
                .addTag("batch_$batchId")
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}