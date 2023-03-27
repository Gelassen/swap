package ru.home.swap.di

import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import ru.home.swap.MainActivity
import ru.home.swap.TestActivity
import ru.home.swap.ui.chains.ChainsFragment
import ru.home.swap.ui.contacts.ContactsFragment
import ru.home.swap.ui.demands.DemandsFragment
import ru.home.swap.ui.offers.OffersFragment
import ru.home.swap.ui.profile.AddItemBottomSheetDialogFragment
import ru.home.swap.ui.profile.LauncherFragment
import ru.home.swap.ui.profile.ProfileFragment
import ru.home.swap.ui.profile.SignInFragment

@Module(
    includes = [
        AndroidSupportInjectionModule::class,
        AndroidInjectionModule::class
    ]
)
abstract class InjectorModule {

    @ContributesAndroidInjector
    abstract fun provideSignInFragmentInjector(): SignInFragment

    @ContributesAndroidInjector
    abstract fun provideLauncherFragmentInjector(): LauncherFragment

    @ContributesAndroidInjector
    abstract fun provideAddItemBottomSheetDialogFragmentInjector(): AddItemBottomSheetDialogFragment

    @ContributesAndroidInjector
    abstract fun provideProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideOffersFragmentInjector(): OffersFragment

    @ContributesAndroidInjector
    abstract fun provideDemandsFragmentInjector(): DemandsFragment

    @ContributesAndroidInjector
    abstract fun provideContactsFragment(): ContactsFragment

    @ContributesAndroidInjector
    abstract fun provideChainsFragment(): ChainsFragment

    @ContributesAndroidInjector
    abstract fun provideMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun provideTestActivity(): TestActivity
}