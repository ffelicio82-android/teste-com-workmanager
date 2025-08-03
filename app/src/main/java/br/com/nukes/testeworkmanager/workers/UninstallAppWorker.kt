package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.usecases.DeleteByPackageNameUseCase
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UninstallAppWorker(
    context: Context,
    params: WorkerParameters,
    private val deleteByPackageNameUseCase: DeleteByPackageNameUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString("data") ?: throw IllegalArgumentException("AppModel is required")
        return@lazy Json.decodeFromString<AppModel>(json)
    }

    override val key: String = "${TAG}_${appModel.packageName.replace(".", "_")}"

    override val stopExecutionByKey: Boolean = true

    override suspend fun executeWork(): WorkerResult {
        return deleteByPackageNameUseCase(appModel.packageName).fold(
            onSuccess = { WorkerResult.Success },
            onFailure = { error ->
                when (error) {
                    is IllegalArgumentException -> WorkerResult.Retry()
                    else -> WorkerResult.Failure
                }
            }
        )
    }

    override fun nextWorker() {
        val request = OneTimeWorkRequest.Builder(FinalizationProcessAppsWorker::class.java)
            .addTag(FinalizationProcessAppsWorker.TAG)
            .addTag(
                "${FinalizationProcessAppsWorker.TAG}_${TAG}_${appModel.packageName.replace(".", "_")}"
            )
            .addTag(DEFAULT_TAG)
            .build()
        workManager.enqueue(request)
    }

    companion object {
        const val TAG = "uninstall_app_worker"
    }
}