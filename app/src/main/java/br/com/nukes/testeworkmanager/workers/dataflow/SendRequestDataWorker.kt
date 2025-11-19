package br.com.nukes.testeworkmanager.workers.dataflow

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.core.NetworkException
import br.com.nukes.testeworkmanager.core.ParseException
import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel
import br.com.nukes.testeworkmanager.domain.usecases.FetchConfigurationsUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllUseCase
import br.com.nukes.testeworkmanager.domain.usecases.SyncDataUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.RetryReason
import br.com.nukes.testeworkmanager.workers.WorkerResult
import br.com.nukes.testeworkmanager.workers.appManagement.ProcessAppsWorker
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SendRequestDataWorker(
    context: Context,
    params: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase,
    private val fetchConfigurationsUseCase: FetchConfigurationsUseCase,
    private val getAllUseCase: GetAllUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    override val key: String = TAG

    private val configurations: ConfigurationsModel by lazy {
        runBlocking {
            fetchConfigurationsUseCase().getOrElse {
                ConfigurationsModel(retryAttempts = 3, intervalAttempts = 60, syncFrequency = 60)
            }
        }
    }

    override fun getRetryLimit(): Int = configurations.retryAttempts
    override fun getIntervalRetry(): Long = configurations.intervalAttempts

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}", "Executing work $TAG in ${System.currentTimeMillis()}")

        return syncDataUseCase().fold(
            onSuccess = { WorkerResult.Success() },
            onFailure = { error ->
                when (error) {
                    is NetworkException.UnauthorizedException -> WorkerResult.Retry(RetryReason.Unauthorized)
                    is NetworkException.TimeoutException,
                    is NetworkException.GatewayTimeoutException -> WorkerResult.Retry(RetryReason.Timeout)
                    is ParseException,
                    is NetworkException.NotFoundException -> WorkerResult.Failure()
                    else -> WorkerResult.Retry()
                }
            }
        )
    }

    override suspend fun nextWorker(data: Data?) {
        val apps = getAllUseCase().getOrElse { emptyList() }
        val batchId = System.currentTimeMillis().toString()

        Log.i("Fernando-tag_${TAG}", "Executing nextWorker in $TAG with ${apps.size} apps for batch $batchId")

        if (apps.isEmpty()) {
            workManager.enqueueUniqueWork(
                "${SendNotificationWorker.TAG}_$batchId",
                ExistingWorkPolicy.KEEP,
                SendNotificationWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId))
            )
            return
        }

        workManager.enqueueUniqueWork(
            "${ProcessAppsWorker.TAG}_$batchId",
            ExistingWorkPolicy.REPLACE,
            ProcessAppsWorker.configureRequest(batchId, workDataOf(BATCH_ID to batchId))
        )

        Log.i("Fernando-tag_${TAG}", "Executing nextWorker in $TAG for batch $batchId")
    }

    override suspend fun onAttemptsExhausted(data: Data?) {
        Log.i("Fernando-tag_${TAG}", "Attempts exhausted for work $key")
    }

    companion object {
        const val TAG = "send_request_data_worker"

        fun configureRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequest.Builder(SendRequestDataWorker::class.java)
                .addTag(TAG)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}