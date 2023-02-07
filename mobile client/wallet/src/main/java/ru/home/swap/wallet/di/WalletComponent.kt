package ru.home.swap.wallet.di

import ru.home.swap.wallet.TestWalletActivity
import dagger.Component
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class WalletMainScope

@WalletMainScope
@Component(modules = [WalletModule::class, WalletViewModelModule::class])
interface WalletComponent {
    fun inject(subj: TestWalletActivity)
}