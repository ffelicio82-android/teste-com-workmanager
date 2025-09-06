package br.com.nukes.testeworkmanager.domain.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class HourlyUsageModel(
    @EncodeDefault var hour: Int = 0,
    @EncodeDefault var foregroundTime: Long = 0,
    @EncodeDefault var backgroundTime: Long = 0,
    @EncodeDefault var launches: Int = 0
)