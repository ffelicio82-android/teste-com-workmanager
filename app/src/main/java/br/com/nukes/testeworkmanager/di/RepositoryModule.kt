package br.com.nukes.testeworkmanager.di

import br.com.nukes.testeworkmanager.data.repository.AppRepositoryImpl
import br.com.nukes.testeworkmanager.data.repository.ConfigurationsRepositoryImpl
import br.com.nukes.testeworkmanager.data.repository.DownloadRepositoryImpl
import br.com.nukes.testeworkmanager.data.repository.SyncRepositoryImpl
import br.com.nukes.testeworkmanager.domain.repository.AppRepository
import br.com.nukes.testeworkmanager.domain.repository.ConfigurationsRepository
import br.com.nukes.testeworkmanager.domain.repository.DownloadRepository
import br.com.nukes.testeworkmanager.domain.repository.SyncRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<SyncRepository> {
        SyncRepositoryImpl(
            get(),
            get(named("IO"))
        )
    }

    single<AppRepository> {
        AppRepositoryImpl(
            get(),
            get(named("IO"))
        )
    }

    single<DownloadRepository> {
        DownloadRepositoryImpl(get())
    }

    single<ConfigurationsRepository> { ConfigurationsRepositoryImpl(get()) }
}