package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.component.inject

class FinalizationProcessAppsWorker(
    context: Context,
    params: WorkerParameters,
    private val getAllAppsUseCase: GetAllAppsUseCase,
    private val fetchByPackageNameUseCase: FetchByPackageNameUseCase
) : BaseWorker(context, params) {

    private val workManager: WorkManager by inject()

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        return getAllAppsUseCase.invoke().fold(
            onSuccess = { apps ->
                when (apps.isEmpty()) {
                    true -> WorkerResult.Success
                    false -> WorkerResult.Failure
                }
            },
            onFailure = { WorkerResult.Success }
        )
    }

    override fun nextWorker() {
        val build = runBlocking { fetchByPackageNameUseCase.invoke("build") }
        lateinit var request: OneTimeWorkRequest

        build
            .onSuccess { app ->
                app?.let {
                    val input = workDataOf("data" to Json.encodeToString(app))
                    request = OneTimeWorkRequest.Builder(DownloadWorker::class.java)
                        .setInputData(input)
                        .addTag("${DownloadWorker.TAG}_${it.packageName.trim()}")
                        .addTag(DownloadWorker.TAG)
                        .addTag(DEFAULT_TAG)
                        .build()
                } ?: run {
                    request = OneTimeWorkRequest.Builder(SendDataWorker::class.java)
                        .addTag(SendDataWorker.TAG)
                        .addTag(DEFAULT_TAG)
                        .build()
                }
                workManager.enqueue(request)
            }
            .onFailure {
                request = OneTimeWorkRequest.Builder(SendDataWorker::class.java)
                    .addTag(SendDataWorker.TAG)
                    .addTag(DEFAULT_TAG)
                    .build()
            }

        workManager.enqueue(request)
    }

    companion object Companion {
        const val TAG = "finalization_process_apps_worker"
    }
}