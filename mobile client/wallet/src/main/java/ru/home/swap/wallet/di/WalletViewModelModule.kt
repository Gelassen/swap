package ru.home.swap.wallet.di

import androidx.lifecycle.ViewModel
import ru.home.swap.wallet.WalletViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.home.swap.core.di.ViewModelKey
import javax.inject.Singleton


@Module
abstract class WalletViewModelModule {


    @WalletMainScope
    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    abstract fun profileViewModel(vm: WalletViewModel): ViewModel

}