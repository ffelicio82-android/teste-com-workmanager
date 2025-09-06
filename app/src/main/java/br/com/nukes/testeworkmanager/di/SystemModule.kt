package br.com.nukes.testeworkmanager.di

import br.com.nukes.testeworkmanager.data.system.packageManager.PackageManagerDataSource
import br.com.nukes.testeworkmanager.data.system.packageManager.PackageManagerDataSourceImpl
import br.com.nukes.testeworkmanager.data.system.usageStats.UsageStatsDataSource
import br.com.nukes.testeworkmanager.data.system.usageStats.UsageStatsDataSourceImpl
import org.koin.dsl.module

val systemModule = module {
    single<PackageManagerDataSource> { PackageManagerDataSourceImpl(get()) }
    single<UsageStatsDataSource> { UsageStatsDataSourceImpl(get()) }
}