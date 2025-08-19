package br.com.nukes.testeworkmanager.domain.repository

import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.models.DownloadEvent
import kotlinx.coroutines.flow.Flow

fun interface DownloadRepository {
    fun downloadFile(appModel: AppModel): Flow<DownloadEvent>
}