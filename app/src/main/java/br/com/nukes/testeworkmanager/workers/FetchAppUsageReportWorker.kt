package br.com.nukes.testeworkmanager.workers

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.usecases.GetAppUsageReportUseCase
import br.com.nukes.testeworkmanager.workers.RetryReason.NoUsagePermission
import br.com.nukes.testeworkmanager.workers.WorkerResult.Retry
import br.com.nukes.testeworkmanager.workers.WorkerResult.Success
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FetchAppUsageReportWorker(
    private val context: Context,
    params: WorkerParameters,
    private val getAppUsageReportUseCase: GetAppUsageReportUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        Log.i(TAG, "Executing work ${System.currentTimeMillis()} - Has permission: ${hasUsageAccessPermission()}")

        if (!hasUsageAccessPermission()) {
            context.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return Retry(NoUsagePermission)
        }

        return try {
            val json = Json {
                encodeDefaults = true   // <- força escrever 0, "", false etc.
                ignoreUnknownKeys = true
            }

            getAppUsageReportUseCase().fold(
                onSuccess = { usageReport ->
                    Log.i(TAG, json.encodeToString(usageReport))
                    Success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to fetch usage report: ${error.message}", error)
                    Retry()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing work: ${e.message}", e)
            throw e
        }
    }

    override suspend fun nextWorker(data: Data?) {
        workManager.enqueue(SendRequestDataWorker.configureRequest())
    }

    private fun hasUsageAccessPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    companion object Companion {
        const val TAG = "fetch_app_usage_report_worker"

        fun configureRequest(): OneTimeWorkRequest  {
            return OneTimeWorkRequest.Builder(FetchAppUsageReportWorker::class.java)
                .addTag(TAG)
                .addTag(DEFAULT_TAG)
                .build()
        }
    }
}