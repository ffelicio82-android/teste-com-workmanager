package br.com.nukes.testeworkmanager.domain.models

data class DataResponseModel(
    val configurations: ConfigurationsModel,
    val apps: List<AppModel>?
)
