package ru.home.swap.di

import dagger.Component
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.ui.contacts.ContactsFragment
import ru.home.swap.ui.demands.DemandsFragment
import ru.home.swap.ui.offers.OffersFragment
import ru.home.swap.ui.profile.AddItemBottomSheetDialogFragment
import ru.home.swap.ui.profile.LauncherFragment
import ru.home.swap.ui.profile.ProfileFragment
import ru.home.swap.ui.profile.SignInFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [
    ViewModelModule::class,
    NetworkModule::class,
    AppModule::class
])
interface AppComponent {
    fun inject(subject: SignInFragment)
    fun inject(subject: LauncherFragment)
    fun inject(subject: AddItemBottomSheetDialogFragment)
    fun inject(subject: ProfileFragment)
    fun inject(subject: OffersFragment)
    fun inject(subject: DemandsFragment)
    fun inject(subject: ContactsFragment)
}