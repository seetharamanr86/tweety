package eu.micer.tweety.presentation.di

import eu.micer.tweety.presentation.vm.TweetListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TweetListViewModel(get()) }
}
