package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.InstalledAppModel
import br.com.nukes.testeworkmanager.domain.repository.SystemRepository

class GetInstalledAppsUseCase(private val systemRepository: SystemRepository) {
    suspend operator fun invoke(): Result<List<InstalledAppModel>> {
        return systemRepository.getInstalledApps()
    }
}