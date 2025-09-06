package br.com.nukes.testeworkmanager.data.system.entities

data class InstalledApp(
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
