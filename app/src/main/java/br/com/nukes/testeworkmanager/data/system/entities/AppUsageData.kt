package br.com.nukes.testeworkmanager.data.system.entities

data class AppUsageData(
    val packageName: String,
    val appName: String,
    val hourlyData: List<HourlyUsageData>,
    val totalForegroundTime: Long = 0L,
    val totalBackgroundTime: Long = 0L
)
