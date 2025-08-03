package br.com.nukes.testeworkmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import org.koin.core.component.KoinComponent

class Worker4(
    context: Context,
    params: WorkerParameters,
    private val fetchByPackageNameUseCase: FetchByPackageNameUseCase
) : BaseWorker(context, params), KoinComponent {

    override val key: String = TAG

    override suspend fun executeWork(): WorkerResult {
        TODO("Not yet implemented")
    }

    companion object {
        const val TAG = "worker_4"
    }
}