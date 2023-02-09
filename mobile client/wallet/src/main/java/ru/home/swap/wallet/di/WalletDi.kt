package ru.home.swap.wallet.di

import android.app.Application
import ru.home.swap.core.di.CoreModule
import ru.home.swap.core.di.DaggerCoreComponent
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.wallet.di.DaggerWalletComponent

class WalletDi(context:  Application) {

    private val walletComponent: WalletComponent

    init {
        // TODO temp solution for di in module should be redesigned for a correct one
        walletComponent = DaggerWalletComponent.builder()
            .walletModule(WalletModule(context))
            .coreComponent(
                DaggerCoreComponent
                    .builder()
                    .coreModule(CoreModule(context))
                    .networkModule(NetworkModule(context))
                    .build()
            )
            .build()
    }

    fun getWalletComponent(): WalletComponent {
        return walletComponent
    }
}