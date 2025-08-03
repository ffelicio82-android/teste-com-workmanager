package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.ConfigurationsModel
import br.com.nukes.testeworkmanager.domain.repository.ConfigurationsRepository

class FetchConfigurationsUseCase(private val configurationsRepository: ConfigurationsRepository) {
    suspend operator fun invoke(): Result<ConfigurationsModel> {
        return configurationsRepository.fetchConfigurations()
    }
}