package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class InstallBuildWorker(
    context: Context,
    params: WorkerParameters,
) : BaseWorker(context, params) {
    override val key: String = TAG

    override fun executeWork(): WorkerResult {
        return try {
            // faz a consulta para pegar os apps a serem instalados (desconsidera RFAL e build)

            // percorre a lista de apps e instala cada um

            // armazena os resultados que deram sucesso ou falha

            Log.i(TAG, "Executing work ${System.currentTimeMillis()}")
            WorkerResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error executing work: ${e.message}", e)
            WorkerResult.Retry
        }
    }

    override fun nextWorker() {
        val request = OneTimeWorkRequest.Builder(SendNotifyWorker::class.java)
            .addTag(SendNotifyWorker.TAG)
            .addTag(DEFAULT_TAG)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    companion object {
        const val TAG = "install_build_worker"
    }
}