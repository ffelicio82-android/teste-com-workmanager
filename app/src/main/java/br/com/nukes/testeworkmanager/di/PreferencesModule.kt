package br.com.nukes.testeworkmanager.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import br.com.nukes.testeworkmanager.data.local.preferences.PreferencesDataSource
import br.com.nukes.testeworkmanager.data.local.preferences.datastore.DatastoreDataSourceImpl
import br.com.nukes.testeworkmanager.data.local.preferences.datastore.syncDataStore
import org.koin.dsl.module

val preferencesModule = module {
    // Caso queira usar SharedPreferences ao invés de DataStore, descomente a linha abaixo
    // single<SharedPreferences> { provideSharedPreferences(get()) }
    // single<PreferencesDataSource> { SharedPreferencesDataSourceImpl(get()) }

    single<DataStore<Preferences>> { get<Context>().syncDataStore }
    single<PreferencesDataSource> { DatastoreDataSourceImpl(get()) }
}