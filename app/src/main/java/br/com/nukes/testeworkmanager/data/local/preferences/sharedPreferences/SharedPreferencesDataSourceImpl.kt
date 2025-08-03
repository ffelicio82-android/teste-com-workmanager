package br.com.nukes.testeworkmanager.data.local.preferences.sharedPreferences

import android.content.SharedPreferences
import br.com.nukes.testeworkmanager.data.local.preferences.Configurations
import br.com.nukes.testeworkmanager.data.local.preferences.PreferencesDataSource
import br.com.nukes.testeworkmanager.utils.Constants
import java.util.concurrent.TimeUnit

class SharedPreferencesDataSourceImpl(private val sharedPreferences : SharedPreferences): PreferencesDataSource {

    override suspend fun saveConfigurations(configurations: Configurations): Boolean {
        val success = sharedPreferences.edit()
            .putLong(SYNC_FREQUENCY_KEY, configurations.syncFrequency)
            .putInt(ATTEMPTS_NUMBER_KEY, configurations.retryAttempts)
            .putLong(ATTEMPTS_DELAY_KEY, configurations.intervalAttempts)
            .commit()
        return success
    }

    override suspend fun fetchConfigurations(): Configurations {
        return Configurations(
            syncFrequency = sharedPreferences.getLong(SYNC_FREQUENCY_KEY, ONE_MINUTE_MILLISECONDS),
            retryAttempts = sharedPreferences.getInt(ATTEMPTS_NUMBER_KEY, DEFAULT_RETRY_LIMIT),
            intervalAttempts = sharedPreferences.getLong(ATTEMPTS_DELAY_KEY, HALF_MINUTE_MILLISECONDS)
        )
    }

    override fun clearConfigurations(): Boolean {
        return true
    }

    companion object Companion {
        private const val PREF_NAME = "configurations"
        private const val SYNC_FREQUENCY_KEY = "sync_frequency"
        private const val ATTEMPTS_DELAY_KEY = "attempts_delay"
        private const val ATTEMPTS_NUMBER_KEY = "attempts_number"
        const val DEFAULT_RETRY_LIMIT = 3

        val TEN_SECONDS_MILLISECONDS = TimeUnit.SECONDS.toMillis(10)
        val HALF_MINUTE_MILLISECONDS = TimeUnit.SECONDS.toMillis(30) // 30 seconds
        val ONE_MINUTE_MILLISECONDS = TimeUnit.MINUTES.toMillis(Constants.ONE)
        val FIVE_MINUTES_MILLISECONDS = TimeUnit.MINUTES.toMillis(5)
        val TWENTY_FOUR_MINUTES_MILLISECONDS = TimeUnit.MINUTES.toMillis(24)
    }
}