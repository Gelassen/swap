package com.example.wallet.di

import com.example.wallet.MintTokenActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [WalletModule::class, WalletViewModelModule::class])
interface WalletComponent {
    fun inject(subj: MintTokenActivity)
}