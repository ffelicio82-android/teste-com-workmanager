package br.com.nukes.testeworkmanager.data.system.entities

data class SystemUsageData(
    val hourlyUsageData: List<HourlyUsageData>,
    var totalForegroundTime: Long = 0,
    var totalBackgroundTime: Long = 0,
    val apps: MutableList<AppUsageData> = mutableListOf()
)
