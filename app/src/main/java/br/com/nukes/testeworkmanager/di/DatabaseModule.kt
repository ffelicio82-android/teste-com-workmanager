package br.com.nukes.testeworkmanager.di

import androidx.room.Room
import br.com.nukes.testeworkmanager.data.local.db.AppDatabase
import br.com.nukes.testeworkmanager.data.local.db.dao.AppDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    single<AppDao> { get<AppDatabase>().appDao() }
}