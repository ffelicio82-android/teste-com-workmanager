package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.domain.models.AppModel
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString("data") ?: throw IllegalArgumentException("AppModel is required")
        return@lazy Json.decodeFromString<AppModel>(json)
    }

    override val key: String = "${TAG}_${appModel.packageName.replace(".", "_")}"

    override suspend fun executeWork(): WorkerResult {
        return WorkerResult.Success
    }

    override fun nextWorker() {
        val request: OneTimeWorkRequest = when (appModel.packageName.contains("build")) {
            true -> {
                OneTimeWorkRequest.Builder(InstallBuildWorker::class.java)
                    .addTag(InstallBuildWorker.TAG)
                    .addTag("${InstallBuildWorker.TAG}_${appModel.packageName.replace(".", "_")}")
            }
            else -> {
                OneTimeWorkRequest.Builder(InstallAppWorker::class.java)
                    .addTag(InstallAppWorker.TAG)
                    .addTag("${InstallAppWorker.TAG}_${appModel.packageName.replace(".", "_")}")
            }
        }
        .setInputData(workDataOf("data" to Json.encodeToString(appModel)))
        .addTag(DEFAULT_TAG)
        .build()


        workManager.enqueue(request)
    }

    companion object {
        const val TAG = "download_worker"
    }
}