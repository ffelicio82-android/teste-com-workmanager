package br.com.nukes.testeworkmanager.workers.appManagement

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.usecases.GetInstalledAppsUseCase
import br.com.nukes.testeworkmanager.utils.Constants
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.WorkerResult
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FetchInstalledAppsWorker(
    context: Context,
    params: WorkerParameters,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        return try {
            getInstalledAppsUseCase().fold(
                onSuccess = {
                    WorkerResult.Success(
                        workDataOf(
                            Constants.DATA to Json.encodeToString(
                                it
                            )
                        )
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Error fetching installed apps", error)
                    WorkerResult.Retry()
                }
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun nextWorker(data: Data?) {
        workManager.enqueue(FetchAppUsageReportWorker.configureRequest())
    }

    override fun finishAllExecutions(callInRetry: Boolean) {
        super.finishAllExecutions(callInRetry)

        if (callInRetry) {
            Log.e(TAG, "Error executing work, call in retry process...")
        } else {
            Log.e(TAG, "Error executing work, finishing...")
        }
    }

    companion object Companion {
        const val TAG = "fetch_installed_apps_worker"

        fun configureRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(FetchInstalledAppsWorker::class.java)
                .addTag(TAG)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}