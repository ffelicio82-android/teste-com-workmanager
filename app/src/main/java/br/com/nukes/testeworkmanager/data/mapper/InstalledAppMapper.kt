package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.system.entities.InstalledApp
import br.com.nukes.testeworkmanager.domain.models.InstalledAppModel

fun InstalledApp.toModel(): InstalledAppModel {
    return InstalledAppModel(
        packageName = packageName,
        label = label,
        versionName = versionName,
        versionCode = versionCode,
        firstInstallTime = firstInstallTime,
        lastUpdateTime = lastUpdateTime,
        isSystemApp = isSystemApp,
        enabled = enabled,
        suspended = suspended
    )
}
