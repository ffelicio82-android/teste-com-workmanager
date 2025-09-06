package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.HourlyUsageData
import br.com.nukes.testeworkmanager.domain.models.HourlyUsageModel
import br.com.nukes.testeworkmanager.utils.extensions.toMinutes

fun HourlyUsageData.toModel(): HourlyUsageModel {
    return HourlyUsageModel(
        hour = this.hour,
        foregroundTime = this.foregroundTime.toMinutes(),
        backgroundTime = this.backgroundTime.toMinutes()
    )
}