package br.com.nukes.testeworkmanager.data.repository

import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import br.com.nukes.testeworkmanager.data.remote.DownloadDataSource
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.models.DownloadEvent
import br.com.nukes.testeworkmanager.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import br.com.nukes.testeworkmanager.data.remote.RawDownloadEvent
import kotlinx.coroutines.flow.onEach

class DownloadRepositoryImpl(private val downloadDataSource: DownloadDataSource) : DownloadRepository {
    override fun downloadFile(appModel: AppModel): Flow<DownloadEvent> {
        return downloadDataSource.download(appModel.url!!, getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS))
            .map { event ->
                when (event) {
                    is RawDownloadEvent.Started -> DownloadEvent.Started
                    is RawDownloadEvent.Progress -> {
                        val total = event.total.coerceAtLeast(1L)
                        val pct = ((event.read * 100) / total).toInt()
                        DownloadEvent.Progress(event.read, total, pct)
                    }
                    is RawDownloadEvent.Completed -> DownloadEvent.Completed(event.absolutePath)
                }
            }
            .onEach { downloadEvent ->
                if (downloadEvent is DownloadEvent.Completed) {
                    // Here you can handle the completion event, e.g., log it or update UI
                    println("Download completed: ${downloadEvent.filePath}")
                }
            }
    }
}