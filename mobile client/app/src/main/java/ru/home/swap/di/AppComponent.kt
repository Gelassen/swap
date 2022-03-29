package ru.home.swap.di

import dagger.Component
import ru.home.swap.ui.main.MainFragment
import ru.home.swap.ui.profile.AddItemBottomSheetDialogFragment
import ru.home.swap.ui.profile.LauncherFragment
import ru.home.swap.ui.profile.SignInFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [ViewModelModule::class, NetworkModule::class, AppModule::class])
interface AppComponent {
    fun inject(subject: MainFragment)
    fun inject(subject: SignInFragment)
    fun inject(subject: LauncherFragment)
    fun inject(subject: AddItemBottomSheetDialogFragment)
}