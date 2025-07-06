package br.com.nukes.testeworkmanager.models

data class AppModel(
    val packageName: String,
    val versionName: String,
    val url: String,
    val action: String,
) {
    override fun toString(): String {
        return "AppModel(packageName='$packageName', versionName='$versionName', url='$url', action='$action')"
    }

    companion object {
        fun getDefaultApps(): List<AppModel> = listOf(
            AppModel("com.exemplo.app1", "1.0.0", "https://exemplo.com/app1", "INSTALL"),
            AppModel("com.exemplo.app2", "2.1.3", "https://exemplo.com/app2", "UPDATE"),
            AppModel("com.exemplo.app3", "3.0.5", "https://exemplo.com/app3", "OPEN"),
            AppModel("com.exemplo.app4", "1.2.0", "https://exemplo.com/app4", "INSTALL"),
        )
    }
}
