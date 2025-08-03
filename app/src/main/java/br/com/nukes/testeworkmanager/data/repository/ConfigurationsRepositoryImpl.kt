package br.com.nukes.testeworkmanager.data.repository

import br.com.nukes.testeworkmanager.core.safeCall
import br.com.nukes.testeworkmanager.data.local.preferences.PreferencesDataSource
import br.com.nukes.testeworkmanager.data.mapper.toConfigurations
import br.com.nukes.testeworkmanager.data.mapper.toModel
import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel
import br.com.nukes.testeworkmanager.domain.repository.ConfigurationsRepository

class ConfigurationsRepositoryImpl(private val preferencesDataSource: PreferencesDataSource) : ConfigurationsRepository {
    override suspend fun saveConfigurations(configurations: ConfigurationsModel): Result<Boolean> = safeCall {
        preferencesDataSource.saveConfigurations(configurations.toConfigurations())
    }

    override suspend fun fetchConfigurations(): Result<ConfigurationsModel> = safeCall {
        preferencesDataSource.fetchConfigurations().toModel()
    }
}