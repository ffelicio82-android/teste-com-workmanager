package br.com.nukes.testeworkmanager.data.repository

import br.com.nukes.testeworkmanager.core.safeCall
import br.com.nukes.testeworkmanager.data.local.db.dao.AppDao
import br.com.nukes.testeworkmanager.data.mapper.toEntity
import br.com.nukes.testeworkmanager.data.mapper.toModel
import br.com.nukes.testeworkmanager.domain.models.AppModel
import br.com.nukes.testeworkmanager.domain.repository.AppRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AppRepositoryImpl(
    private val appDao: AppDao,
    private val ioDispatcher: CoroutineDispatcher
) : AppRepository {
    override suspend fun save(apps: List<AppModel>): Result<Unit> = safeCall {
        withContext(ioDispatcher) {
            appDao.insert( apps.map { app -> app.toEntity() })
        }
    }

    override suspend fun getAll(): Result<List<AppModel>> = safeCall {
        withContext(ioDispatcher) {
            appDao.getAll().map { app -> app.toModel() }
        }
    }

    override suspend fun fetchByPackageName(packageName: String): Result<AppModel?> = safeCall {
        withContext(ioDispatcher) {
            appDao.getByPackageName(packageName)?.toModel()
        }
    }

    override suspend fun deleteByPackageName(packageName: String) = safeCall {
        appDao.deleteByPackageName(packageName)
    }
}