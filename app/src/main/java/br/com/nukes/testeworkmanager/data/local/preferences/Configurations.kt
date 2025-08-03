package br.com.nukes.testeworkmanager.data.local.preferences

data class Configurations(
    val retryAttempts: Int,
    val intervalAttempts: Long,
    val syncFrequency: Long
)
