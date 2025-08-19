package br.com.nukes.testeworkmanager.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.usecases.DeleteByPackageNameUseCase
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InstallBuildWorker(
    context: Context,
    params: WorkerParameters,
    private val deleteByPackageNameUseCase: DeleteByPackageNameUseCase
) : BaseWorker(context, params), KoinComponent {

    private val workManager: WorkManager by inject()

    private val appModel: AppModel by lazy {
        val json = inputData.getString("data") ?: throw IllegalArgumentException("AppModel is required")
        Json.decodeFromString<AppModel>(json)
    }

    private val batchId by lazy { inputData.getString("batchId") ?: "no_batch" }
    private val pkgSafe by lazy { appModel.packageName.replace(".", "_") }

    override val key: String = "${TAG}_${batchId}_$pkgSafe"

    override suspend fun executeWork(): WorkerResult {
        Log.i("Fernando-tag_$TAG}", "Executing install build work ${appModel.packageName} in batch $batchId")

        return WorkerResult.Success()
    }

    companion object {
        const val TAG = "install_build_worker"
    }
}