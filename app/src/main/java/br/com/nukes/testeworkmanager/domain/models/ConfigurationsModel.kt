package br.com.nukes.testeworkmanager.domain.models

data class ConfigurationsModel(
    val retryAttempts: Int,
    val intervalAttempts: Long,
    val syncFrequency: Long
)
