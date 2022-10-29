package ru.home.swap.core.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import ru.home.swap.core.di.ViewModelFactory

@Module
abstract class CoreViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory //You are able to declare ViewModelProvider.Factory dependency in another module. For example in ApplicationModule.
}