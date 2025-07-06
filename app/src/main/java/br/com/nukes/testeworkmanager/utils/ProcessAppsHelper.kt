package br.com.nukes.testeworkmanager.utils

import android.content.Context
import android.content.SharedPreferences
import br.com.nukes.testeworkmanager.workers.Step

class ProcessAppsHelper(private val context: Context) {
    private val sharedPreferences : SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveStep(key: String, step: Step) {
        sharedPreferences.edit().apply {
            putString("step_$key", step.name)
            apply()
        }
    }

    fun fetchStep(key: String): Step? {
        val stepName = sharedPreferences.getString("step_$key", null) ?: return null
        return Step.valueOf(stepName)
    }

    fun resetStep(key: String) {
        sharedPreferences.edit().apply {
            remove("step_$key")
            apply()
        }
    }

    companion object Companion {
        private const val PREF_NAME = "process_apps"
    }
}