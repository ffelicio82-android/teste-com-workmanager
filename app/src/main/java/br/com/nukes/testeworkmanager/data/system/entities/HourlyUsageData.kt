package br.com.nukes.testeworkmanager.data.system.entities

data class HourlyUsageData(
    val hour: Int,
    val foregroundTime: Long = 0L,
    val backgroundTime: Long = 0L
)