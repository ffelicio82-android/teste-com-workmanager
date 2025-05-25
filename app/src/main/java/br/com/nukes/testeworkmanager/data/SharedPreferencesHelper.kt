package br.com.nukes.testeworkmanager.data

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("is_first_run", true)
    }

    fun setFirstRun(isFirstRun: Boolean) {
        sharedPreferences.edit { putBoolean("is_first_run", isFirstRun) }
    }

    fun getInterval(defaultValue: Long = 1L): Long {
        return sharedPreferences.getLong("interval", defaultValue)
    }

    fun saveServerConfig(interval: Long) {
        sharedPreferences.edit(commit = true) {
            putLong("interval", interval)
        }
    }

    fun saveShutdown(interval: Long) {
        sharedPreferences.edit(commit = true) {
            putLong("shutdown_in", interval)
        }
    }
}