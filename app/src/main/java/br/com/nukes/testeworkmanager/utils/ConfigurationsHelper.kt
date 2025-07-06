package br.com.nukes.testeworkmanager.utils

import android.content.Context
import android.content.SharedPreferences

class ConfigurationsHelper(private val context: Context) {
    private val sharedPreferences : SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun fetchActualRetry(key: String): Int {
        return sharedPreferences.getInt("retry_$key", 0)
    }

    fun saveRetry(key: String, retry: Int) {
        sharedPreferences.edit().apply {
            putInt("retry_$key", retry)
            apply()
        }
    }

    fun resetRetry(key: String) {
        saveRetry(key, DEFAULT_RETRY)
    }

    fun fetchRetryLimit(key: String): Int {
        return sharedPreferences.getInt("retry_limit_$key", DEFAULT_RETRY_LIMIT)
    }

    fun fetchIntervalRetry(key: String): Long {
        return sharedPreferences.getLong("interval_retry_$key", DEFAULT_INTERVAL_RETRY)
    }

    fun saveConfigurations(key: String, maxRetry: Int, intervalRetry: Long) {
        sharedPreferences.edit().apply {
            putInt("retry_limit_$key", maxRetry)
            putLong("interval_retry_$key", intervalRetry)
            apply()
        }
    }

    fun fetchInterval(): Long {
        return sharedPreferences.getLong("interval", Constants.FIVE_MINUTES_MILLISECONDS)
    }

    companion object Companion {
        private const val PREF_NAME = "configurations"
        private const val DEFAULT_RETRY = 0
        private const val DEFAULT_INTERVAL = 24 * 60 * 1000L // Default to 24 minutes in milliseconds
        private const val DEFAULT_RETRY_LIMIT = 3
        private const val DEFAULT_INTERVAL_RETRY = 5L * 60 * 1000 // Default to 5 minutes in milliseconds
    }
}