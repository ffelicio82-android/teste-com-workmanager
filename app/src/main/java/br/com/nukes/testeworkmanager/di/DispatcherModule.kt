package br.com.nukes.testeworkmanager.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dispatcherModule = module {
    single<CoroutineDispatcher>(qualifier = named("IO")) { Dispatchers.IO }
    single<CoroutineDispatcher>(qualifier = named("Default")) { Dispatchers.Default }
    single<CoroutineDispatcher>(qualifier = named("Main")) { Dispatchers.Main }
}