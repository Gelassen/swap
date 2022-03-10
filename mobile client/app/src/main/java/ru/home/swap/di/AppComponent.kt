package ru.home.swap.di

import dagger.Component
import ru.home.swap.ui.main.MainFragment

@Component(modules = [ViewModelModule::class, NetworkModule::class])
interface AppComponent {
    fun inject(subject: MainFragment)
}