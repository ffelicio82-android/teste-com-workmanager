package br.com.nukes.testeworkmanager.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SystemUsageDataModel(
    val hourlyUsageData: List<HourlyUsageModel>,
    @EncodeDefault val totalForegroundTime : Double = 0.0,
    @EncodeDefault val totalBackgroundTime: Double = 0.0,
    val apps: MutableList<AppUsageDataModel>
)
