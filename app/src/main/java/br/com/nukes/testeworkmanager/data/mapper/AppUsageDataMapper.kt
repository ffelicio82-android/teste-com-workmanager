package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.AppUsageData
import br.com.nukes.testeworkmanager.domain.models.AppUsageDataModel
import br.com.nukes.testeworkmanager.utils.extensions.toMinutes

fun AppUsageData.toModel(): AppUsageDataModel {
    return AppUsageDataModel(
        packageName = this.packageName,
        appName = this.appName,
        hourlyData = this.hourlyData.map { it.toModel() },
        totalForegroundTime = this.totalForegroundTime.toMinutes(),
        totalBackgroundTime = this.totalBackgroundTime.toMinutes()
    )
}