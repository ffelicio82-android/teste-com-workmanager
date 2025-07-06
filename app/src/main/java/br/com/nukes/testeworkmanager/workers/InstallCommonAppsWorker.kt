package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.utils.ProcessAppsHelper

enum class Step {
    DOWNLOAD,
    VALIDATE,
    INSTALL,
    CLEANUP // usado quando o android informa que não tem mais espaço para instalar apps
}

class InstallCommonAppsWorker(
    context: Context,
    params: WorkerParameters,
) : BaseWorker(context, params) {

    private val processAppsHelper : ProcessAppsHelper by lazy { ProcessAppsHelper(context) }

    private val packageName = inputData.getString("package_name") ?: "unknown"
    private var actualStep : Step
        get() {
            return processAppsHelper.fetchStep(packageName.replace(".", "_")) ?: Step.DOWNLOAD
        }
        set(value) {
            if (packageName.contains("unknown").not()) {
                processAppsHelper.saveStep(packageName.replace(".", "_"), value)
            }
        }

    override val key: String
        get() = "install_${packageName.replace(".", "_")}_${actualStep.name.lowercase()}"

    override fun customMaximumRetry(): Int? {
        return when (actualStep) {
            Step.DOWNLOAD -> 3
            Step.VALIDATE -> 0
            Step.INSTALL -> 2
            Step.CLEANUP -> 3
        }
    }

    override fun customIntervalRetry(): Long? {
        return when (actualStep) {
            Step.DOWNLOAD -> 30_000L
            Step.VALIDATE -> 0L
            Step.INSTALL -> 60_000L
            Step.CLEANUP -> 30_000L
        }
    }

    override fun executeWork(): WorkerResult {
        val status = WorkerResult.Success
        Log.d(TAG, "Status-for-app $packageName: $status")
        return status
    }

    companion object {
        const val TAG = "install_common_apps_worker"
    }
}