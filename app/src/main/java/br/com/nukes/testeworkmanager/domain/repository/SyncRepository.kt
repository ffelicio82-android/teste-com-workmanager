package br.com.nukes.testeworkmanager.domain.repository

import br.com.nukes.testeworkmanager.domain.models.DataResponseModel

fun interface SyncRepository {
    suspend fun fetchData() : Result<DataResponseModel?>
}