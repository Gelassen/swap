package ru.home.swap.wallet.di

import ru.home.swap.wallet.MintTokenActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [WalletModule::class, WalletViewModelModule::class])
interface WalletComponent {
    fun inject(subj: MintTokenActivity)
}