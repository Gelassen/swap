package com.example.wallet.debug.di

import com.example.wallet.debug.MintTokenActivity
import com.example.wallet.debug.WalletViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [WalletModule::class, WalletViewModelModule::class])
interface WalletComponent {
    fun inject(subj: MintTokenActivity)
}