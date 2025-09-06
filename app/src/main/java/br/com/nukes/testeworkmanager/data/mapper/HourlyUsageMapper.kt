package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.HourlyUsageData
import br.com.nukes.testeworkmanager.domain.models.HourlyUsageModel

fun HourlyUsageData.toModel(): HourlyUsageModel {
    return HourlyUsageModel(
        hour = this.hour,
        foregroundTime = this.foregroundTime,
        backgroundTime = this.backgroundTime,
        launches = this.launches
    )
}