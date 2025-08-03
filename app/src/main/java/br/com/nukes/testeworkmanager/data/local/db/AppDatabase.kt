package br.com.nukes.testeworkmanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.nukes.testeworkmanager.data.local.db.dao.AppDao
import br.com.nukes.testeworkmanager.data.local.db.entities.AppEntity

@Database(
    entities = [AppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        const val DATABASE_NAME = "app.db"
    }
}