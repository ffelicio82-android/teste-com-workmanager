package br.com.nukes.testeworkmanager.data.system.packageManager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import br.com.nukes.testeworkmanager.data.system.entities.InstalledApp
import br.com.nukes.testeworkmanager.data.system.extensions.isSystemApp

class PackageManagerDataSourceImpl(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager
): PackageManagerDataSource {
    override fun getInstalledApps(): List<InstalledApp> {
        val mainLauncherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolves = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(mainLauncherIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainLauncherIntent, 0)
        }

        val packageNames = resolves
            .filter { resolveInfo ->
                resolveInfo.activityInfo != null &&
                resolveInfo.activityInfo.enabled &&
                resolveInfo.activityInfo.applicationInfo.enabled
            }
            .mapNotNull { it.activityInfo.packageName }
            .toSet()

        return packageNames.mapNotNull { pkg ->
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                val label = appInfo.loadLabel(packageManager).toString()
                val packageInfo: PackageInfo = packageManager.getPackageInfo(pkg, 0)

                InstalledApp(
                    packageName = pkg,
                    label = label,
                    versionName = packageInfo.versionName,
                    versionCode = packageInfo.longVersionCode,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    isSystemApp = appInfo.isSystemApp(),
                    enabled = appInfo.enabled,
                    suspended = isSuspended(pkg),
                )
            } catch (_: Throwable) {
                null
            }
        }.sortedWith(compareBy { it.label.lowercase() })
    }

    private fun isSuspended(packageName: String): Boolean {
        return try {
            packageManager.isPackageSuspended(packageName)
        } catch (_: Throwable) {
            // Alguns fabricantes lançam SecurityException se não for permitido checar
            false
        }
    }
}