package br.com.nukes.testeworkmanager.data.repository

import br.com.nukes.testeworkmanager.core.safeIo
import br.com.nukes.testeworkmanager.data.local.db.dao.AppDao
import br.com.nukes.testeworkmanager.data.mapper.toEntity
import br.com.nukes.testeworkmanager.data.mapper.toModel
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineDispatcher

class AppRepositoryImpl(
    private val appDao: AppDao,
    private val dispatcher: CoroutineDispatcher
) : AppRepository {
    override suspend fun save(apps: List<AppModel>): Result<Unit> = safeIo(dispatcher) {
        appDao.insert( apps.map { app -> app.toEntity() })
    }

    override suspend fun getAll(): Result<List<AppModel>> = safeIo(dispatcher) {
        appDao.getAll().map { app -> app.toModel() }
    }

    override suspend fun getAllApps(): Result<List<AppModel>> = safeIo(dispatcher) {
        appDao.getAllApps().map { app -> app.toModel() }
    }

    override suspend fun fetchByPackageName(packageName: String): Result<AppModel?> = safeIo(dispatcher) {
        appDao.getByPackageName(packageName)?.toModel()
    }

    override suspend fun deleteByPackageName(packageName: String) = safeIo(dispatcher) {
        appDao.deleteByPackageName(packageName)
    }
}