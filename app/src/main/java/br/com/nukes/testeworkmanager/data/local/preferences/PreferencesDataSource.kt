package br.com.nukes.testeworkmanager.data.local.preferences

interface PreferencesDataSource {
    suspend fun saveConfigurations(configurations: Configurations): Boolean
    suspend fun fetchConfigurations(): Configurations
    fun clearConfigurations(): Boolean
}