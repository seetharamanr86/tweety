package eu.micer.tweety.app.di

import eu.micer.tweety.data.local.di.databaseModule
import eu.micer.tweety.data.remote.di.networkModule
import eu.micer.tweety.domain.di.repositoryModule
import eu.micer.tweety.presentation.di.processorModule
import eu.micer.tweety.presentation.di.viewModelModule
import org.koin.dsl.module

/**
 * KOIN keywords:
 * single — declare a singleton instance component (unique instance)
 * factory — declare a factory instance component (new instance on each demand)
 * bind — declare an assignable class/interface to the provided component
 * get — retrieve a component, for provided definition function
 */

val appModule = module {
    // nothing here yet
}

// Gather all app modules
val allModules = listOf(
    appModule,
    networkModule,
    databaseModule,
    processorModule,
    repositoryModule,
    viewModelModule
)
