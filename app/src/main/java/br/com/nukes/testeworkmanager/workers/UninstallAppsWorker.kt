package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.models.AppModel

class UninstallAppsWorker(
    context: Context,
    params: WorkerParameters,
) : BaseWorker(context, params) {
    override val key: String = TAG

    override fun executeWork(): WorkerResult {
        return try {
            // faz a consulta para pegar os apps a serem desinstalados

            // percorre a lista de apps e desinstala cada um

            // armazena os resultados que deram sucesso ou falha

            Log.i(TAG, "Executing work ${System.currentTimeMillis()}")
            WorkerResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error executing work: ${e.message}", e)
            WorkerResult.Retry
        }
    }

    override fun nextWorker() {
        val workerRequests = AppModel.getDefaultApps().map { app ->
            OneTimeWorkRequestBuilder<InstallCommonAppsWorker>()
                .setInputData(
                    workDataOf(
                        "package_name" to app.packageName,
                        "version_name" to app.versionName,
                        "url" to app.url,
                        "action" to app.action
                    )
                )
                .addTag(InstallCommonAppsWorker.TAG)
                .addTag(app.packageName)
                .addTag(DEFAULT_TAG)
                .build()
        }.chunked(2)

        val workManager = WorkManager.getInstance(applicationContext)
        var continuation = workManager.beginWith(workerRequests.first())

        workerRequests.drop(1).forEach { workRequest ->
            continuation = continuation.then(workRequest)
        }

        continuation = continuation.then(OneTimeWorkRequest.from(InstallRfalAppWorker::class.java))
        continuation.enqueue()
    }

    companion object {
        const val TAG = "uninstall_apps_worker"
    }
}