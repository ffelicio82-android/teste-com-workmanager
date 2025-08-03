package br.com.nukes.testeworkmanager.data.remote

import br.com.nukes.testeworkmanager.data.remote.dto.DataResponseDto

fun interface SyncRemoteDataSource {
    suspend fun fetchUrls(): DataResponseDto?
}