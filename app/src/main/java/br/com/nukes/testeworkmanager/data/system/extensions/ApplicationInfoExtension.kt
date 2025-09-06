package br.com.nukes.testeworkmanager.data.system.extensions

import android.content.pm.ApplicationInfo

fun ApplicationInfo.isSystemApp(): Boolean {
    return (
        (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
        (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
    )
}