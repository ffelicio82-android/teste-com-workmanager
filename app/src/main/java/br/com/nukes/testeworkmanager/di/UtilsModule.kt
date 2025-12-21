package br.com.nukes.testeworkmanager.di

import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController
import org.koin.dsl.module

val utilsModule = module {
    single { PipelineController() }
}