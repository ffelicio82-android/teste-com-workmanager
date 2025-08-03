package br.com.nukes.testeworkmanager.data.repository

import br.com.nukes.testeworkmanager.core.safeCall
import br.com.nukes.testeworkmanager.data.mapper.toModel
import br.com.nukes.testeworkmanager.data.remote.SyncRemoteDataSource
import br.com.nukes.testeworkmanager.domain.models.DataResponseModel
import br.com.nukes.testeworkmanager.domain.repository.SyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SyncRepositoryImpl(
    private val remoteDataSource: SyncRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher
): SyncRepository {
    override suspend fun fetchData(): Result<DataResponseModel?> = safeCall {
        withContext(ioDispatcher) {
            remoteDataSource.fetchUrls()?.toModel()
        }
    }
}