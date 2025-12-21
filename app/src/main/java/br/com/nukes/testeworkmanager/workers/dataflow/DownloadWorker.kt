package br.com.nukes.testeworkmanager.workers.dataflow

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
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineStep
import br.com.nukes.testeworkmanager.workers.BaseWorker
import br.com.nukes.testeworkmanager.workers.RetryReason
import br.com.nukes.testeworkmanager.workers.WorkerResult
import br.com.nukes.testeworkmanager.workers.appManagement.InstallAppWorker
import br.com.nukes.testeworkmanager.workers.system.InstallBuildWorker
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
    private val downloadUseCase: DownloadUseCase,
    private val pipelineController: PipelineController
) : BaseWorker(context, params, pipelineController), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString(DATA) ?: throw IllegalArgumentException("AppModel is required")
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString(BATCH_ID) ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }
    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override val step = PipelineStep.DOWNLOAD

    override suspend fun executeWork(): WorkerResult {
        val packageName = inputData.getString("packageName")

        Log.i("Fernando-tag_${TAG}", "Executing work download $packageName in batch $batchId")

        return try {
            downloadUseCase(appModel)
                .onEach { downloadEvent ->
                    when (downloadEvent) {
                        is DownloadEvent.Started -> {
                            pipelineController.stepProgress(PipelineStep.DOWNLOAD_STARTED, appModel, 0)

                            setProgress(
                                workDataOf(
                                    BATCH_ID to batchId,
                                    PACKAGE_NAME to appModel.packageName,
                                    PROGRESS to 0
                                )
                            )
                            Log.i("Fernando-tag_${TAG}", "Download started for ${appModel.packageName} in batch $batchId")
                        }
                        is DownloadEvent.Progress -> {
                            val processStep = if (downloadEvent.percent < 100) {
                                PipelineStep.DOWNLOADING
                            } else {
                                PipelineStep.DOWNLOADED
                            }

                            pipelineController.stepProgress(processStep, appModel, downloadEvent.percent)

                            setProgress(
                                workDataOf(
                                    BATCH_ID to batchId,
                                    PACKAGE_NAME to appModel.packageName,
                                    PROGRESS to downloadEvent.percent
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

    override suspend fun nextWorker(data: Data?) {
        val json = Json.encodeToString(appModel)
        val input = workDataOf(DATA to json, BATCH_ID to batchId)

        val workRequest = when (appModel.packageName) {
            InstallBuildWorker.BUILD -> InstallBuildWorker.configureRequest(batchId, input, pkgSafe)
            else -> InstallAppWorker.configureRequest(batchId, input, pkgSafe)
        }

        Log.i("Fernando-tag_${TAG}", "Enqueuing next ${InstallAppWorker.TAG} for ${appModel.packageName} in batch $batchId")
        workManager.enqueue(workRequest)
    }

    override suspend fun onAttemptsExhausted(data: Data?) {
        super.onAttemptsExhausted(data)

        // remove from DB?
        Log.i("Fernando-tag_${TAG}}", "onAttemptsExhausted ${appModel.packageName} in batch $batchId")
    }

    private fun mapToWorkerResult(t: Throwable): WorkerResult {
        return when (t) {
            is SocketTimeoutException -> WorkerResult.Retry(RetryReason.SocketTimeout)
            is UnknownHostException,
            is ConnectException -> WorkerResult.Retry(RetryReason.NetworkUnreachable)
            is IOException -> WorkerResult.Retry(RetryReason.IoTransient)
            else -> WorkerResult.Failure()
        }
    }

    companion object {
        const val TAG = "download_worker"
        private const val PROGRESS = "progress"
        private const val PACKAGE_NAME = "packageName"

        fun configureRequest(batchId: String?, input: Data?, pkgSafe: String?): OneTimeWorkRequest {
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .addTag(TAG)
                .addTag(DEFAULT_TAG)

            batchId?.let { request.addTag("batch_$it") }
            pkgSafe?.let { request.addTag("${TAG}_$it") }
            input?.let { data -> request.setInputData(data) }

            return request.build()
        }
    }
}