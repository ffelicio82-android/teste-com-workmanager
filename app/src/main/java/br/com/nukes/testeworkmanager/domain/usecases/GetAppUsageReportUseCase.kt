package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.domain.models.SystemUsageDataModel
import br.com.nukes.testeworkmanager.domain.repository.SystemRepository

class GetAppUsageReportUseCase(private val systemRepository: SystemRepository) {
    suspend operator fun invoke(): Result<SystemUsageDataModel> {
        return systemRepository.getUsageReport()
    }
}