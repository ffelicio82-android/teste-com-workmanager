package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.repository.AppRepository

class DeleteByPackageNameUseCase(private val appRepository: AppRepository) {
    suspend operator fun invoke(packageName: String): Result<Unit> {
        return appRepository.deleteByPackageName(packageName)
    }
}