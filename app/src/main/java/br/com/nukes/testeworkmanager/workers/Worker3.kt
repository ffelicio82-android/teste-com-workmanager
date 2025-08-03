package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
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
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import br.com.nukes.testeworkmanager.domain.usecases.SyncDataUseCase
import br.com.nukes.testeworkmanager.workers.RetryReason.Timeout
import br.com.nukes.testeworkmanager.workers.RetryReason.Unauthorized
import br.com.nukes.testeworkmanager.workers.WorkerResult.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class Worker3(
    context: Context,
    params: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase,
    private val fetchConfigurationsUseCase: FetchConfigurationsUseCase,
    private val getAllAppsUseCase: GetAllAppsUseCase
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
            onSuccess = { Success },
            onFailure = { error ->
                when (error) {
                    is UnauthorizedException -> Retry(Unauthorized)
                    is TimeoutException -> Retry(Timeout)
                    is GatewayTimeoutException -> Retry(Timeout)

                    is ParseException -> Failure
                    is NotFoundException -> Failure

                    else -> Retry()
                }
            }
        )
    }

    override fun nextWorker() {
        val apps = runBlocking { getAllAppsUseCase() }.getOrElse { emptyList() }

        when (apps.isEmpty()) {
            true -> {
                val request = OneTimeWorkRequest.Builder(FinalizationProcessAppsWorker::class.java)
                    .addTag(FinalizationProcessAppsWorker.TAG)
                    .addTag(DEFAULT_TAG)
                    .build()
                workManager.enqueue(request)
            }
            else -> {
                val workRequests = apps.map { app ->
                    val input = workDataOf("data" to Json.encodeToString(app))
                    lateinit var tag: String

                    val workRequest = when (app.action.uppercase(Locale.ROOT)) {
                        INSTALL_FLAG -> {
                            tag = DownloadWorker.TAG
                            OneTimeWorkRequestBuilder<DownloadWorker>()
                        }
                        else -> {
                            tag = UninstallAppWorker.TAG
                            OneTimeWorkRequestBuilder<UninstallAppWorker>()
                        }
                    }

                    workRequest
                        .setInputData(input)
                        .addTag("${tag}_${app.packageName.trim()}")
                        .addTag(tag)
                        .addTag(DEFAULT_TAG)
                        .build()
                }

                workManager.enqueue(workRequests)
            }
        }
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
        const val TAG = "worker_3"
        const val INSTALL_FLAG = "I"
    }
}