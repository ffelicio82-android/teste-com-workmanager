package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import org.koin.core.component.inject

class FinalizationProcessAppsWorker(
    context: Context,
    params: WorkerParameters,
    private val getAllAppsUseCase: GetAllAppsUseCase,
    private val fetchByPackageNameUseCase: FetchByPackageNameUseCase
) : BaseWorker(context, params) {

    private val workManager: WorkManager by inject()

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}", "Executing $TAG in batch $batchId")


        /*return getAllAppsUseCase.invoke().fold(
            onSuccess = { apps ->
                when (apps.isEmpty()) {
                    true -> WorkerResult.Success
                    false -> WorkerResult.Failure
                }
            },
            onFailure = { WorkerResult.Success }
        )*/

        return WorkerResult.Success()
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