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
import br.com.nukes.testeworkmanager.domain.models.DownloadEvent
import br.com.nukes.testeworkmanager.domain.usecases.DownloadUseCase
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.workers.RetryReason.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class DownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadUseCase: DownloadUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString(DATA) ?: throw IllegalArgumentException("AppModel is required")
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }
    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_${TAG}", "Executing work download ${appModel.packageName} in batch $batchId")

        if (appModel.packageName == "facebook.katana") {
            return WorkerResult.Retry(SocketTimeout)
        }

        return try {
            downloadUseCase(appModel)
                .onEach { downloadEvent ->
                    when (downloadEvent) {
                        is DownloadEvent.Started -> {
                            setProgress(
                                workDataOf(
                                    "batchId" to batchId,
                                    "packageName" to appModel.packageName,
                                    "progress" to 0
                                )
                            )
                            Log.i("Fernando-tag_${TAG}", "Download started for ${appModel.packageName} in batch $batchId")
                        }
                        is DownloadEvent.Progress -> {
                            setProgress(
                                workDataOf(
                                    "batchId" to batchId,
                                    "packageName" to appModel.packageName,
                                    "progress" to downloadEvent.percent
                                )
                            )
                            Log.i("Fernando-tag_${TAG}", "Download progress for ${appModel.packageName} in batch $batchId - ${downloadEvent.percent}%")
                        }
                        is DownloadEvent.Completed -> {
                            Log.d("Fernando-tag_${TAG}", "Download completed for ${appModel.packageName}")
                        }
                    }

                    if (isStopped) throw CancellationException("Cancelled by WorkManager")
                }
                .collect()

            WorkerResult.Success()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            mapToWorkerResult(t)
        }
    }

    override fun nextWorker(data: Data?) {
        val json = Json.encodeToString(appModel)
        val input = workDataOf(DATA to json, BATCH_ID to batchId)

        Log.i("Fernando-tag_${TAG}", "Enqueuing next ${InstallAppWorker.TAG} for ${appModel.packageName} in batch $batchId")
        workManager.enqueue(InstallAppWorker.configureRequest(batchId, input, pkgSafe))
    }

    override fun onAttemptsExhausted(data: Data?) {
        super.onAttemptsExhausted(data)

        // remove from DB
        Log.i("Fernando-tag_${TAG}}", "onAttemptsExhausted ${appModel.packageName} in batch $batchId")

        // val json = Json.encodeToString(appModel)
        // val input = workDataOf(DATA to json, BATCH_ID to batchId)
        // workManager.enqueue(FinalizationProcessAppsWorker.configureRequest(batchId, input, pkgSafe))
    }

    private fun mapToWorkerResult(t: Throwable): WorkerResult {
        return when (t) {
            is SocketTimeoutException -> WorkerResult.Retry(SocketTimeout)
            is UnknownHostException,
            is ConnectException -> WorkerResult.Retry(NetworkUnreachable)
            is IOException -> WorkerResult.Retry(IoTransient)
            else -> WorkerResult.Failure()
        }
    }

    companion object {
        const val TAG = "download_worker"

        fun configureRequest(batchId: String, input: Data, pkgSafe: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(input)
                .addTag(TAG)
                .addTag("${TAG}_$pkgSafe")
                .addTag("batch_$batchId")
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}