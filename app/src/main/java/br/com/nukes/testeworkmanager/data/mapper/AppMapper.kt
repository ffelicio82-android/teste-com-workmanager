package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.remote.dto.AppDto
import br.com.nukes.testeworkmanager.data.local.db.entities.AppEntity
import br.com.nukes.testeworkmanager.domain.models.AppModel

fun AppModel.toEntity(): AppEntity {
    return AppEntity(
        packageName = packageName,
        action = action,
        url = url
    )
}

fun AppDto.toModel(): AppModel {
    return AppModel(
        packageName = packageName,
        action = action,
        url = url
    )
}

fun AppEntity.toModel(): AppModel {
    return AppModel(
        packageName = packageName,
        action = action,
        url = url
    )
}