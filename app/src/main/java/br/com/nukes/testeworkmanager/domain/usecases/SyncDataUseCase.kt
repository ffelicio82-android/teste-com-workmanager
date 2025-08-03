package br.com.nukes.testeworkmanager.domain.usecases

import br.com.nukes.testeworkmanager.core.UnknownException
import br.com.nukes.testeworkmanager.domain.repository.AppRepository
import br.com.nukes.testeworkmanager.domain.repository.ConfigurationsRepository
import br.com.nukes.testeworkmanager.domain.repository.SyncRepository

class SyncDataUseCase(
    private val syncRepository: SyncRepository,
    private val appRepository: AppRepository,
    private val configurationsRepository: ConfigurationsRepository
) {
    suspend operator fun invoke() : Result<Unit> {
        return try {
            val syncResult = syncRepository.fetchData()

            if (syncResult.isFailure) {
                return Result.failure(syncResult.exceptionOrNull() ?: UnknownException())
            }

            when (val data = syncResult.getOrThrow()) {
                null -> Result.success(Unit)
                else -> {
                    // Save configurations and apps if they are not null or empty
                    configurationsRepository.saveConfigurations(data.configurations)

                    if (data.apps.isNullOrEmpty().not()) {
                        appRepository.save(data.apps)
                    }

                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}