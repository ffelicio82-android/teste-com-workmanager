package br.com.nukes.testeworkmanager.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class HourlyUsageModel(
    @EncodeDefault var hour: Int = 0,
    @EncodeDefault var foregroundTime: Double = 0.0,
    @EncodeDefault var backgroundTime: Double = 0.0
)