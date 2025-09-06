package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.SystemUsageData
import br.com.nukes.testeworkmanager.domain.models.SystemUsageDataModel
import br.com.nukes.testeworkmanager.utils.extensions.toMinutes

fun SystemUsageData.toModel() : SystemUsageDataModel {
    return SystemUsageDataModel(
        hourlyUsageData = this.hourlyUsageData.map { it.toModel() },
        totalForegroundTime = this.totalForegroundTime.toMinutes(),
        totalBackgroundTime = this.totalBackgroundTime.toMinutes(),
        apps = this.apps.map { it.toModel() }.toMutableList()
    )
}