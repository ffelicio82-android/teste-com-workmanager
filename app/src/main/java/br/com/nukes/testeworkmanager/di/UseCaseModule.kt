package br.com.nukes.testeworkmanager.di

import br.com.nukes.testeworkmanager.domain.usecases.DeleteByPackageNameUseCase
import br.com.nukes.testeworkmanager.domain.usecases.DownloadUseCase
import br.com.nukes.testeworkmanager.domain.usecases.FetchByPackageNameUseCase
import br.com.nukes.testeworkmanager.domain.usecases.FetchConfigurationsUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllAppsUseCase
import br.com.nukes.testeworkmanager.domain.usecases.GetAllUseCase
import br.com.nukes.testeworkmanager.domain.usecases.SyncDataUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { SyncDataUseCase(get(), get(), get()) }
    factory { FetchConfigurationsUseCase(get()) }
    factory { GetAllUseCase(get()) }
    factory { GetAllAppsUseCase(get()) }
    factory { FetchByPackageNameUseCase(get()) }
    factory { DeleteByPackageNameUseCase(get()) }
    factory { DownloadUseCase(get()) }
}