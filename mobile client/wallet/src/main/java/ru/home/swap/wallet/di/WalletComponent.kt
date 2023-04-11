package ru.home.swap.wallet.di

import androidx.work.WorkManager
import ru.home.swap.wallet.TestWalletActivity
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.wallet.WalletApplication
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.dao.ChainTransactionDao
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WalletMainScope

@WalletMainScope
@Component(
    dependencies = [CoreComponent::class],
    modules = [WalletModule::class, WalletViewModelModule::class])
interface WalletComponent {
    fun inject(subj: TestWalletActivity)
    fun inject(subject: WalletApplication)
    fun providesWalletRepository(): IWalletRepository
    fun providesStorageRepository(): IStorageRepository
    fun providesChainTransactionDao(): ChainTransactionDao
    @Named(WalletModule.CACHE_SCOPE)
    fun providesCacheUtilsScope(): CoroutineScope
    fun providesWorkManager(): WorkManager
}