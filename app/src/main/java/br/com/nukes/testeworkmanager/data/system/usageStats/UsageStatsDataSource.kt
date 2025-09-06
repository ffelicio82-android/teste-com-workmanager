package br.com.nukes.testeworkmanager.data.system.usageStats

import br.com.nukes.testeworkmanager.data.system.entities.SystemUsageData

fun interface UsageStatsDataSource {
    fun getUsageReport(installedApps: List<String>): SystemUsageData
}