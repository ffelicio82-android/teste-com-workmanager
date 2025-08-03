package br.com.nukes.testeworkmanager.domain.repository

import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel

interface ConfigurationsRepository {
    suspend fun saveConfigurations(configurations: ConfigurationsModel): Result<Boolean>
    suspend fun fetchConfigurations(): Result<ConfigurationsModel>
}