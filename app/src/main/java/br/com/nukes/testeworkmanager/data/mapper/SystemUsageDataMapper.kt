package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.SystemUsageData
import br.com.nukes.testeworkmanager.domain.models.SystemUsageDataModel

fun SystemUsageData.toModel() : SystemUsageDataModel {
    return SystemUsageDataModel(
        hourlyUsageData = this.hourlyUsageData.map { it.toModel() },
        totalForegroundTime = this.totalForegroundTime,
        totalBackgroundTime = this.totalBackgroundTime,
        totalLaunches = this.totalLaunches,
        apps = this.apps.map { it.toModel() }.toMutableList()
    )
}