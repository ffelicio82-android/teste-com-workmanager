package br.com.nukes.testeworkmanager.data.mapper

import br.com.nukes.testeworkmanager.data.remote.dto.DataResponseDto
import br.com.nukes.testeworkmanager.domain.models.DataResponseModel

fun DataResponseDto.toModel(): DataResponseModel {
    return DataResponseModel(
        configurations = configurations.toModel(),
        apps = apps?.map { it.toModel() }
    )
}