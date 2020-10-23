package eu.micer.tweety.domain.di

import eu.micer.tweety.domain.repository.TweetRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { TweetRepository(get(), get()) }
}
