package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.utils.ConfigurationsHelper
import br.com.nukes.testeworkmanager.utils.Constants.ONE
import br.com.nukes.testeworkmanager.workers.SendRequestDataWorker.Companion.TAG
import java.util.concurrent.TimeUnit.MILLISECONDS

sealed class WorkerResult {
    object Success : WorkerResult()
    object Retry : WorkerResult()
    object Failure : WorkerResult()
}

abstract class BaseWorker(
    private val context: Context,
    params: WorkerParameters,
    private val configurationsHelper: ConfigurationsHelper = ConfigurationsHelper(context)
) : CoroutineWorker(context, params) {
    abstract val key: String
    open fun customMaximumRetry(): Int? = null
    open fun customIntervalRetry(): Long? = null

    abstract fun executeWork() : WorkerResult

    open fun nextWorker() {
        // este método pode ser sobrescrito para definir o próximo Worker a ser executado
    }

    override suspend fun doWork(): Result {
        val actualRetry = configurationsHelper.fetchActualRetry(key)
        val retryLimit = customMaximumRetry() ?: configurationsHelper.fetchRetryLimit(key)
        val intervalRetry = customIntervalRetry() ?: configurationsHelper.fetchIntervalRetry(key)

        return try {
            when(executeWork()) {
                is WorkerResult.Success -> {
                    Log.i(TAG, "Work completed successfully for $key")
                    configurationsHelper.resetRetry(key)

                    // Chama o próximo Worker
                    nextWorker()

                    Result.success()
                }
                is WorkerResult.Retry -> {
                    applyRetryHandling(actualRetry, retryLimit, intervalRetry)
                    Result.failure()
                }
                is WorkerResult.Failure -> {
                    Log.e(TAG, "Critical error. Cannot recover from $key")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during work for $key: ${e.message}", e)
            applyRetryHandling(actualRetry, retryLimit, intervalRetry)
            Result.failure()
        }
    }

    private fun applyRetryHandling(actualRetry: Int, retryLimit: Int, intervalRetry: Long) {
        val newRetry = actualRetry.plus(ONE.toInt())

        if (newRetry >= retryLimit) {
            configurationsHelper.resetRetry(key)
        } else {
            configurationsHelper.saveRetry(key, newRetry)

            val retryRequest = OneTimeWorkRequest.Builder(this::class.java)
                .setInitialDelay(intervalRetry, MILLISECONDS)
                .addTag(key)
                .addTag(DEFAULT_TAG)
                .build()

            WorkManager.getInstance(context).enqueue(retryRequest)
        }
    }

    companion object {
        const val DEFAULT_TAG = "worker"
    }
}