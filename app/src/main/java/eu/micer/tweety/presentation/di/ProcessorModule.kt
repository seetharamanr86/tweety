package eu.micer.tweety.presentation.di

import eu.micer.tweety.presentation.processor.TweetListActionProcessorHolder
import org.koin.dsl.module

val processorModule = module {
    single { TweetListActionProcessorHolder(get()) }
}
