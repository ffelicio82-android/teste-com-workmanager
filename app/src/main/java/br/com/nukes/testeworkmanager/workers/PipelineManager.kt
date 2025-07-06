package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.com.nukes.testeworkmanager.utils.ConfigurationsHelper
import br.com.nukes.testeworkmanager.utils.Constants.HALF_MINUTE_MILLISECONDS
import br.com.nukes.testeworkmanager.utils.Constants.ONE_MINUTE_MILLISECONDS
import br.com.nukes.testeworkmanager.utils.Constants.THREE
import br.com.nukes.testeworkmanager.utils.Constants.ZERO

object PipelineManager {
    private val fixedWorkers = listOf(
        Triple(InitialWorker.TAG, ZERO.toInt(), ZERO),
        Triple(FetchInstalledAppsWorker.TAG, THREE, HALF_MINUTE_MILLISECONDS),
        Triple(FetchManufacturerDataWorker.TAG, THREE, HALF_MINUTE_MILLISECONDS),
        Triple(ProcessAppsWorker.TAG, THREE, ONE_MINUTE_MILLISECONDS),
    )

    fun registerFixedWorkers(context: Context) {
        val configurationsHelper = ConfigurationsHelper(context)
        fixedWorkers.forEach { (key, maxRetry, intervalRetry) ->
            configurationsHelper.saveConfigurations(key, maxRetry, intervalRetry)
        }
    }

    fun initialize(context: Context) {
        val initialWorker = OneTimeWorkRequestBuilder<InitialWorker>()
            .addTag(BaseWorker.DEFAULT_TAG)
            .addTag(InitialWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueue(initialWorker)
    }
}