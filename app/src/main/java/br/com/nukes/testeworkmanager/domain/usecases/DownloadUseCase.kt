package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.models.DownloadEvent
import br.com.nukes.testeworkmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class DownloadUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(appModel: AppModel): Flow<DownloadEvent> {
        return downloadRepository.downloadFile(appModel)
    }
}