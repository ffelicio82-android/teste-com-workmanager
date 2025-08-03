package br.com.nukes.testeworkmanager.data.remote.dto

import com.squareup.moshi.Json

data class DataResponseDto(
    val configurations: ConfigurationsDto,
    @Json(name = "apps") val apps: List<AppDto>?,
)

data class ConfigurationsDto(
    @Json(name = "retry_attempts") val retryAttempts: Int,
    @Json(name = "interval_attempts") val intervalAttempts: Long,
    @Json(name = "sync_frequency") val syncFrequency: Long
)

data class AppDto(
    @Json(name = "package_name") val packageName: String,
    @Json(name = "action") val action: String,
    @Json(name = "url") val url: String? = null,
)