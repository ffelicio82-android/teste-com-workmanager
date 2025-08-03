package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.repository.AppRepository

class GetAllAppsUseCase(private val appRepository: AppRepository) {
    suspend operator fun invoke(): Result<List<AppModel>> {
        return appRepository.getAll()
    }
}