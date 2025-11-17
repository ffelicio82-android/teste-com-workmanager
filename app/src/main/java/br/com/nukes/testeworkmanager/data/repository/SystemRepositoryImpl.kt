package br.com.nukes.testeworkmanager.data.repository

import br.com.nukes.testeworkmanager.core.safeIo
import br.com.nukes.testeworkmanager.data.mapper.toModel
import br.com.nukes.testeworkmanager.data.system.packageManager.PackageManagerDataSource
import br.com.nukes.testeworkmanager.data.system.usageStats.UsageStatsDataSource
import br.com.nukes.testeworkmanager.domain.models.InstalledAppModel
import br.com.nukes.testeworkmanager.domain.models.SystemUsageDataModel
import br.com.nukes.testeworkmanager.domain.repository.SystemRepository
import kotlinx.coroutines.CoroutineDispatcher

class SystemRepositoryImpl(
    private val packageManagerDataSource: PackageManagerDataSource,
    private val usageStatsDataSource: UsageStatsDataSource,
    private val dispatcher: CoroutineDispatcher
): SystemRepository {
    override suspend fun getInstalledApps(): Result<List<InstalledAppModel>> = safeIo(dispatcher) {
        packageManagerDataSource.getInstalledApps().map { it.toModel() }
    }

    override suspend fun getUsageReport(): Result<SystemUsageDataModel> = safeIo(dispatcher) {
        val installedApps = packageManagerDataSource.getInstalledApps().filter {
            it.packageName.contains("br.com.test")
        }.map {
            it.packageName
        }
        usageStatsDataSource.getUsageReport(installedApps).toModel()
    }
}