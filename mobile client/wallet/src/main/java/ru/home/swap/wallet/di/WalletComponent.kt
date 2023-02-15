package ru.home.swap.wallet.di

import ru.home.swap.wallet.TestWalletActivity
import dagger.Component
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.ChainTransactionDao
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
    fun providesWalletRepository(): IWalletRepository
    fun providesStorageRepository(): IStorageRepository
    fun providesChainTransactionDao(): ChainTransactionDao
}