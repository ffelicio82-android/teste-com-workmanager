package br.com.nukes.testeworkmanager.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AppModel(
    val packageName: String,
    val action: String,
    val url: String? = null
) {
    override fun toString(): String {
        return "AppModel(packageName='$packageName', url='$url', action='$action')"
    }
}
