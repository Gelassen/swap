package com.example.wallet.debug.di

import android.app.Application

class WalletDi(context:  Application) {

    private val walletComponent: WalletComponent

    init {
        // TODO temp solution for di in module should be redesigned for a correct one
        walletComponent = DaggerWalletComponent
            .builder()
            .walletModule(WalletModule(context))
            .build()
    }

    fun getWalletComponent(): WalletComponent {
        return walletComponent
    }
}