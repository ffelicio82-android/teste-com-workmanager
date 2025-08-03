package br.com.nukes.testeworkmanager.data.remote

import br.com.nukes.testeworkmanager.data.remote.dto.DataResponseDto
import retrofit2.http.GET

fun interface SyncApi {
    @GET("v1/sync")
    suspend fun fetchUrls(): List<DataResponseDto?>
}