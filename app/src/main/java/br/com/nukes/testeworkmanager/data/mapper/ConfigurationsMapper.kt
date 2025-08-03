package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.local.preferences.Configurations
import br.com.nukes.testeworkmanager.data.remote.dto.ConfigurationsDto
import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel

fun ConfigurationsDto.toModel(): ConfigurationsModel {
    return ConfigurationsModel(
        retryAttempts = retryAttempts,
        intervalAttempts = intervalAttempts,
        syncFrequency = syncFrequency
    )
}

fun ConfigurationsModel.toConfigurations(): Configurations {
    return Configurations(
        retryAttempts = retryAttempts,
        intervalAttempts = intervalAttempts,
        syncFrequency = syncFrequency
    )
}

fun Configurations.toModel(): ConfigurationsModel {
    return ConfigurationsModel(
        retryAttempts = retryAttempts,
        intervalAttempts = intervalAttempts,
        syncFrequency = syncFrequency
    )
}