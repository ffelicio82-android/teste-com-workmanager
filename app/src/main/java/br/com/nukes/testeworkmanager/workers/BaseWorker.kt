package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.utils.Constants.ONE
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

sealed class WorkerResult {
    object Success : WorkerResult()
    data class Retry(val reason: RetryReason? = null) : WorkerResult()
    object Failure : WorkerResult()
    object Finish : WorkerResult()
}

sealed class RetryReason(val retryLimit: Int, val intervalRetry: Long) {
    object Unauthorized : RetryReason(retryLimit = 1, intervalRetry = 30)
    object Timeout : RetryReason(retryLimit = 3, intervalRetry = 20)
}

abstract class BaseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    abstract val key: String

    open fun getRetryLimit(): Int = 3
    open fun getIntervalRetry(): Long = TimeUnit.MINUTES.toSeconds(5L)

    open val stopExecutionByKey : Boolean = false

    abstract suspend fun executeWork() : WorkerResult

    open fun nextWorker() {
        // este método pode ser sobrescrito para definir o próximo Worker a ser executado
    }

    open fun finishExecutions(callInRetry: Boolean = false) {
        workManager.cancelAllWorkByTag(key)
    }

    // este método pode ser sobrescrito para definir o que fazer em caso de erro
    open fun finishAllExecutions(callInRetry: Boolean = false) {
        workManager.cancelAllWorkByTag(DEFAULT_TAG)
    }

    override suspend fun doWork(): Result {
        return try {
            when (val result = executeWork()) {
                is WorkerResult.Success -> {
                    Log.i(DEFAULT_TAG, "Work completed successfully for $key")

                    // Chama o próximo Worker
                    nextWorker()

                    Result.success()
                }
                is WorkerResult.Retry -> {
                    applyRetryHandling(result.reason)
                    Result.failure()
                }
                is WorkerResult.Failure -> {
                    Log.e(DEFAULT_TAG, "Critical error. Cannot recover from $key")
                    when (stopExecutionByKey) {
                        true -> finishExecutions()
                        false -> finishAllExecutions()
                    }
                    Result.failure()
                }
                is WorkerResult.Finish -> {
                    Log.i(DEFAULT_TAG, "Work finished in $key")
                    when (stopExecutionByKey) {
                        true -> finishExecutions()
                        false -> finishAllExecutions()
                    }
                    Result.success()
                }
            }
        } catch (e: SecurityException) {
            Log.e(DEFAULT_TAG, "Security error during work for $key: ${e.message}", e)
            finishExecutions()
            Result.failure()
        } catch (e: Exception) {
            Log.e(DEFAULT_TAG, "Unexpected error during work for $key: ${e.message}", e)
            applyRetryHandling()
            Result.failure()
        }
    }

    private fun applyRetryHandling(reason: RetryReason? = null) {
        val lastReason = inputData.getString("${key}_last_reason")
        val currentReason = reason?.javaClass?.simpleName

        val actualRetry = when {
            currentReason != lastReason -> ONE.toInt()
            else -> inputData.getInt("${key}_${ACTUAL_RETRY_KEY}", 0).plus(ONE.toInt())
        }

        val data = inputData.getString("data") ?: "No data provided"
        val retryLimit = reason?.retryLimit ?: getRetryLimit()
        val intervalRetry = reason?.intervalRetry ?: getIntervalRetry()

        Log.i(DEFAULT_TAG, "Retry $actualRetry of $retryLimit for $key")

        if (actualRetry > retryLimit) {
            when (stopExecutionByKey) {
                true -> finishExecutions(callInRetry = true)
                false -> finishAllExecutions(callInRetry = true)
            }
        } else {
            val retryRequest = OneTimeWorkRequest.Builder(this::class.java)
                .setInitialDelay(intervalRetry, SECONDS)
                .setInputData(
                    workDataOf(
                        "${key}_${ACTUAL_RETRY_KEY}" to actualRetry,
                        "${key}_last_reason" to currentReason,
                        "data" to data
                    )
                )
                .addTag(key)
                .addTag("retry_$key")
                .addTag(DEFAULT_TAG)
                .build()

            workManager.enqueue(retryRequest)
        }
    }

    companion object {
        const val DEFAULT_TAG = "worker"
        const val ACTUAL_RETRY_KEY = "actualRetry"
    }
}