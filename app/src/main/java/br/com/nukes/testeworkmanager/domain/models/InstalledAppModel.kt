package br.com.nukes.testeworkmanager.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class InstalledAppModel(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long?,
    val firstInstallTime: Long?,
    val lastUpdateTime: Long?,
    val isSystemApp: Boolean,
    val enabled: Boolean,
    val suspended: Boolean,
)
