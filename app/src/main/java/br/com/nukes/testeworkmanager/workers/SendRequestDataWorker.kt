package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.core.NetworkException.GatewayTimeoutException
import br.com.nukes.testeworkmanager.core.NetworkException.NotFoundException
import br.com.nukes.testeworkmanager.core.NetworkException.TimeoutException
import br.com.nukes.testeworkmanager.core.NetworkException.UnauthorizedException
import br.com.nukes.testeworkmanager.core.ParseException
import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel
import br.com.nukes.testeworkmanager.domain.usecases.FetchConfigurationsUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllUseCase
import br.com.nukes.testeworkmanager.domain.usecases.SyncDataUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.workers.RetryReason.Timeout
import br.com.nukes.testeworkmanager.workers.RetryReason.Unauthorized
import br.com.nukes.testeworkmanager.workers.WorkerResult.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
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
            fetchConfigurationsUseCase.invoke().getOrElse {
                ConfigurationsModel(retryAttempts = 3, intervalAttempts = 60, syncFrequency = 60)
            }
        }
    }

    override fun getRetryLimit(): Int = configurations.retryAttempts
    override fun getIntervalRetry(): Long = configurations.intervalAttempts

    override suspend fun executeWork(): WorkerResult {
        Log.i(TAG, "Executing work ${System.currentTimeMillis()}")

        return syncDataUseCase.invoke().fold(
            onSuccess = { Success() },
            onFailure = { error ->
                when (error) {
                    is UnauthorizedException -> Retry(Unauthorized)
                    is TimeoutException,
                    is GatewayTimeoutException -> Retry(Timeout)
                    is ParseException,
                    is NotFoundException -> Failure()
                    else -> Retry()
                }
            }
        )
    }

    override fun nextWorker(data: Data?) {
        val apps = runBlocking { getAllUseCase() }.getOrElse { emptyList() }
        val batchId = System.currentTimeMillis().toString()

        if (apps.isEmpty()) {
            workManager.enqueueUniqueWork(
                "${SendNotificationWorker.TAG}_$batchId",
                ExistingWorkPolicy.KEEP,
                SendNotificationWorker.configureRequest(batchId)
            )
            return
        }

        val regularApps = apps.filterNot { it.packageName.trim().equals("build", true) }

        val perAppContinuations = mutableListOf<WorkContinuation>()

        regularApps.forEach { app ->
            val json = Json.encodeToString(app)
            val input = workDataOf(DATA to json, BATCH_ID to batchId)
            val pkgSafe = app.packageName.trim().replace(".", "_")

            val request = when (app.action.uppercase()) {
                INSTALL_FLAG -> DownloadWorker.configureRequest(batchId, input, pkgSafe)
                else -> UninstallAppWorker.configureRequest(batchId, input, pkgSafe)
            }

            val workContinuation = workManager.beginUniqueWork(
                "process_${pkgSafe}_$batchId",
                ExistingWorkPolicy.REPLACE,
                request
            )
            perAppContinuations += workContinuation
        }

        WorkContinuation.combine(perAppContinuations).enqueue()
    }

    override fun onAttemptsExhausted(data: Data?) {
        super.onAttemptsExhausted(data)
    }

    companion object Companion {
        const val TAG = "send_request_data_worker"
        const val INSTALL_FLAG = "I"
    }
}