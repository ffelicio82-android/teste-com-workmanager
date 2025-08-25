package br.com.nukes.testeworkmanager.data.local.preferences.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import br.com.nukes.testeworkmanager.core.PreferencesException
import br.com.nukes.testeworkmanager.core.PreferencesException.FetchDataException
import br.com.nukes.testeworkmanager.data.local.preferences.Configurations
import br.com.nukes.testeworkmanager.data.local.preferences.PreferencesDataSource
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.TimeUnit

object PreferencesKey {
    val RETRY_ATTEMPTS = intPreferencesKey("retry_attempts")
    val INTERVAL_ATTEMPTS = longPreferencesKey("interval_attempts")
    val SYNC_FREQUENCY = longPreferencesKey("sync_frequency")
}

class DatastoreDataSourceImpl(private val preferences: DataStore<Preferences>): PreferencesDataSource {
    override suspend fun saveConfigurations(configurations: Configurations): Boolean {
        return try {
            preferences.edit { prefs ->
                prefs[PreferencesKey.RETRY_ATTEMPTS] = configurations.retryAttempts
                prefs[PreferencesKey.INTERVAL_ATTEMPTS] = configurations.intervalAttempts
                prefs[PreferencesKey.SYNC_FREQUENCY] = configurations.syncFrequency
            }
            true
        } catch (e: Exception) {
            throw PreferencesException.EditException("Error saving configurations: ${e.message}", e)
        }
    }

    override suspend fun fetchConfigurations(): Configurations {
        return preferences.data
            .catch { e ->
                if (e is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw FetchDataException("Error reading configurations: ${e.message}", e)
                }
            }
            .map { prefs ->
                Configurations(
                    retryAttempts = prefs[PreferencesKey.RETRY_ATTEMPTS] ?: 3,
                    intervalAttempts = prefs[PreferencesKey.INTERVAL_ATTEMPTS]
                        ?: TimeUnit.SECONDS.toSeconds(30L),
                    syncFrequency = prefs[PreferencesKey.SYNC_FREQUENCY]
                        ?: TimeUnit.MINUTES.toSeconds(2L)
                )
            }.first()
    }

    override fun clearConfigurations(): Boolean {
        return try {
            runBlocking {
                preferences.edit { prefs ->
                    prefs.remove(PreferencesKey.RETRY_ATTEMPTS)
                    prefs.remove(PreferencesKey.INTERVAL_ATTEMPTS)
                    prefs.remove(PreferencesKey.SYNC_FREQUENCY)
                }
            }
            true
        } catch (e: Exception) {
            throw PreferencesException.ClearDataException("Error clearing configurations: ${e.message}", e)
        }
    }
}