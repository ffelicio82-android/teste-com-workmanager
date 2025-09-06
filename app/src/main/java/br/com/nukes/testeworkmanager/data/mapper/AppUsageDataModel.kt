package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.AppUsageData
import br.com.nukes.testeworkmanager.domain.models.AppUsageDataModel

fun AppUsageData.toModel(): AppUsageDataModel {
    return AppUsageDataModel(
        packageName = this.packageName,
        hourlyData = this.hourlyData.map { it.toModel() },
        totalForegroundTime = this.totalForegroundTime,
        totalBackgroundTime = this.totalBackgroundTime,
        totalLaunches = this.totalLaunches
    )
}