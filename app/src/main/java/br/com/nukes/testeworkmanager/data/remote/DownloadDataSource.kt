package br.com.nukes.testeworkmanager.data.remote

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import java.io.File

sealed class RawDownloadEvent {
    data object Started : RawDownloadEvent()
    data class Progress(val read: Long, val total: Long) : RawDownloadEvent()
    data class Completed(val absolutePath: String) : RawDownloadEvent()
}

interface DownloadDataSource {
    fun download(url: String, target: File, resume: Boolean = true) : Flow<RawDownloadEvent>
}

class DownloadDataSourceImpl(
    private val client: OkHttpClient,
    private val dispatcher: CoroutineDispatcher
) : DownloadDataSource {
    override fun download(url: String, target: File, resume: Boolean): Flow<RawDownloadEvent> = flow {
        emit(RawDownloadEvent.Started)

        repeat(10) {
            emit(RawDownloadEvent.Progress(it.toLong().plus(1), 10L))
            delay(1000)
        }

        emit(RawDownloadEvent.Completed(target.absolutePath))
    }
    .conflate() // Use conflate to avoid backpressure issues, as download events can be frequent
    .flowOn(dispatcher)
}