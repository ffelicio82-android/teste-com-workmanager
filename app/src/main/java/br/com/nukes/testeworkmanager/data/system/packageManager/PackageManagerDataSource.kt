package br.com.nukes.testeworkmanager.data.system.packageManager

import br.com.nukes.testeworkmanager.data.system.entities.InstalledApp

fun interface PackageManagerDataSource {
    fun getInstalledApps(): List<InstalledApp>
}