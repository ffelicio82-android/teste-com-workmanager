package br.com.nukes.testeworkmanager.data.remote

import br.com.nukes.testeworkmanager.data.remote.dto.DataResponseDto

class SyncRemoteDataSourceImpl(private val syncApi: SyncApi): SyncRemoteDataSource {
    override suspend fun fetchUrls(): DataResponseDto? = syncApi.fetchUrls().first()
}