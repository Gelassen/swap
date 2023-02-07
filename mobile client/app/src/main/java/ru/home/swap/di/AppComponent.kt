package ru.home.swap.di

import dagger.Component
import ru.home.swap.AppApplication
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.wallet.di.WalletComponent
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppMainScope

@AppMainScope
@Component(
    modules = [
        ViewModelModule::class,
//        NetworkModule::class,
        AppModule::class,
        InjectorModule::class
    ],
    dependencies = [
        CoreComponent::class,
        WalletComponent::class
    ]
)
interface AppComponent {
    fun inject(subject: AppApplication)
//    fun inject(subject: SignInFragment)
//    fun inject(subject: LauncherFragment)
//    fun inject(subject: AddItemBottomSheetDialogFragment)
//    fun inject(subject: ProfileFragment)
//    fun inject(subject: OffersFragment)
//    fun inject(subject: DemandsFragment)
//    fun inject(subject: ContactsFragment)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent
        fun coreComponent(coreComponent: CoreComponent): Builder
        fun walletComponent(walletComponent: WalletComponent): Builder
        fun appModule(appModule: AppModule): Builder
    }
}