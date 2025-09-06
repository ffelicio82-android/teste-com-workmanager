package br.com.nukes.testeworkmanager.domain.repository

import br.com.nukes.testeworkmanager.domain.models.InstalledAppModel
import br.com.nukes.testeworkmanager.domain.models.SystemUsageDataModel

interface SystemRepository {
    suspend fun getInstalledApps(): Result<List<InstalledAppModel>>

    suspend fun getUsageReport(): Result<SystemUsageDataModel>
}