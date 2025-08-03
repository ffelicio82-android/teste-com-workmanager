package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.repository.AppRepository

class FetchByPackageNameUseCase(private val appRepository: AppRepository) {
    suspend operator fun invoke(packageName: String): Result<AppModel?> {
        return appRepository.fetchByPackageName(packageName)
    }
}