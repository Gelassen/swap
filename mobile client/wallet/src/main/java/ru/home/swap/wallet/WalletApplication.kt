package ru.home.swap.wallet

import ru.home.swap.core.CoreApplication
import ru.home.swap.wallet.di.DaggerWalletComponent
import ru.home.swap.wallet.di.WalletComponent
import ru.home.swap.wallet.di.WalletModule

open class WalletApplication(): CoreApplication() {

    protected lateinit var walletComponent: WalletComponent

    override fun onCreate() {
        super.onCreate()
        walletComponent = prepareWalletComponent()
        walletComponent.inject(this)
    }

    protected open fun prepareWalletComponent(): WalletComponent {
        return DaggerWalletComponent
            .builder()
            .walletModule(WalletModule(this))
            .coreComponent(coreComponent)
            .build()
    }

}