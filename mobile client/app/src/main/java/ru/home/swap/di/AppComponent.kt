package ru.home.swap.di

import dagger.Component
import ru.home.swap.ui.main.MainFragment
import javax.security.auth.Subject

@Component(modules = [ViewModelModule::class])
interface AppComponent {
    fun inject(subject: MainFragment)
}