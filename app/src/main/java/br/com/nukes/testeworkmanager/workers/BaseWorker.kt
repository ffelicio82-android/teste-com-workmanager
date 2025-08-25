package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import br.com.nukes.testeworkmanager.utils.Constants.BATCH_ID
import br.com.nukes.testeworkmanager.utils.Constants.DATA
import br.com.nukes.testeworkmanager.utils.Constants.ONE
import br.com.nukes.testeworkmanager.utils.Constants.ZERO
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

sealed class WorkerResult {
    data class Success(val data: Data? = null) : WorkerResult()
    data class Retry(val reason: RetryReason? = null, val data: Data? = null) : WorkerResult()
    data class Failure(val data: Data? = null) : WorkerResult()
}

sealed class RetryReason(val retryLimit: Int, val intervalRetry: Long) {
    object Unauthorized : RetryReason(retryLimit = 1, intervalRetry = 30)
    object SocketTimeout : RetryReason(retryLimit = 6, intervalRetry = 3)
    object NetworkUnreachable : RetryReason(retryLimit = 3, intervalRetry = 20)
    object IoTransient : RetryReason(retryLimit = 3, intervalRetry = 20)
    object Timeout : RetryReason(retryLimit = 3, intervalRetry = 20)
    object Database : RetryReason(retryLimit = 2, intervalRetry = 5)
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

    @Suppress("RedundantSuspendModifier")
    protected open suspend fun onBeforeNextWorker() {
        /* override */
    }

    protected open suspend fun nextWorker(data: Data) {
        /* override */
    }

    @Suppress("RedundantSuspendModifier")
    protected open suspend fun nextWorker() {
        /* override */
    }

    protected open fun finishExecutions(callInRetry: Boolean = false) {
        workManager.cancelAllWorkByTag(key)
    }

    protected open fun finishAllExecutions(callInRetry: Boolean = false) {
        workManager.cancelAllWorkByTag(DEFAULT_TAG)
    }

    protected open suspend fun onAttemptsExhausted(data: Data? = null) {
        Log.w("Fernando-tag_${DEFAULT_TAG}", "All retry attempts exhausted for $key")
    }

    override suspend fun doWork(): Result {
        return try {
            when (val result = executeWork()) {
                is WorkerResult.Success -> {
                    Log.i("Fernando-tag_${DEFAULT_TAG}", "Work completed successfully for $key")

                    onBeforeNextWorker()

                    when (result.data) {
                        null -> nextWorker()
                        else -> nextWorker(result.data)
                    }

                    Result.success(result.data ?: workDataOf())
                }
                is WorkerResult.Retry -> {
                    applyRetryHandling(result.reason)
                    Result.success(result.data ?: workDataOf())
                }
                is WorkerResult.Failure -> {
                    Log.e("Fernando-tag_${DEFAULT_TAG}", "Critical error. Cannot recover from $key")
                    if (stopExecutionByKey) finishExecutions() else finishAllExecutions()
                    Result.failure(result.data ?: workDataOf())
                }
            }
        } catch (e: SecurityException) {
            Log.e("Fernando-tag_${DEFAULT_TAG}", "Security error during work for $key: ${e.message}", e)
            finishExecutions()
            Result.failure()
        } catch (e: Exception) {
            Log.e("Fernando-tag_${DEFAULT_TAG}", "Unexpected error during work for $key: ${e.message}", e)
            applyRetryHandling()
            Result.success()
        }
    }

    private suspend fun applyRetryHandling(reason: RetryReason? = null) {
        val lastReason = inputData.getString("${key}_last_reason")
        val currentReason = reason?.javaClass?.simpleName

        val actualRetry = if (currentReason != lastReason) 1
        else inputData.getInt("${key}_${ACTUAL_RETRY_KEY}", ZERO.toInt()) + ONE.toInt()

        val data = inputData.getString("data") ?: "No data provided"
        val batchId = inputData.getString(BATCH_ID) ?: "No batch ID provided"
        val retryLimit = reason?.retryLimit ?: getRetryLimit()
        val intervalRetry = reason?.intervalRetry ?: getIntervalRetry()

        Log.i("Fernando-tag_${DEFAULT_TAG}", "Retry $actualRetry of $retryLimit for $key (reason=$currentReason)")

        when {
            actualRetry > retryLimit -> { onAttemptsExhausted(inputData) }
            else -> {
                val retryRequest = OneTimeWorkRequest.Builder(this::class.java)
                    .setInitialDelay(intervalRetry, SECONDS)
                    .setInputData(
                        workDataOf(
                            "${key}_${ACTUAL_RETRY_KEY}" to actualRetry,
                            "${key}_last_reason" to currentReason,
                            DATA to data,
                            BATCH_ID to batchId
                        )
                    )
                    .addTag(key)
                    .addTag("retry_$key")
                    .addTag(DEFAULT_TAG)
                    .build()

                workManager
                    .beginUniqueWork(key, ExistingWorkPolicy.APPEND_OR_REPLACE, retryRequest)
                    .enqueue()
            }
        }
    }

    companion object {
        const val DEFAULT_TAG = "worker"
        const val ACTUAL_RETRY_KEY = "actualRetry"
    }
}