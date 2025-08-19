package br.com.nukes.testeworkmanager.domain.repository

import br.com.nukes.testeworkmanager.domain.models.AppModel

interface AppRepository {
    suspend fun save(apps: List<AppModel>) : Result<Unit>
    suspend fun getAll(): Result<List<AppModel>>
    suspend fun getAllApps(): Result<List<AppModel>>
    suspend fun fetchByPackageName(packageName: String): Result<AppModel?>
    suspend fun deleteByPackageName(packageName: String) : Result<Unit>
}