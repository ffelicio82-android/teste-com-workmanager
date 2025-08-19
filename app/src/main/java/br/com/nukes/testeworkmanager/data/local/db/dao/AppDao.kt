package br.com.nukes.testeworkmanager.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.nukes.testeworkmanager.data.local.db.entities.AppEntity

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(apps: List<AppEntity>)

    @Query("SELECT * FROM apps")
    suspend fun getAll(): List<AppEntity>

    @Query("SELECT * FROM apps WHERE packageName != :build")
    suspend fun getAllApps(build: String = "build"): List<AppEntity>

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): AppEntity?

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("DELETE FROM apps")
    suspend fun deleteAll()
}