package com.example.wallet.debug.di

import androidx.lifecycle.ViewModel
import com.example.wallet.debug.WalletViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.home.swap.core.di.ViewModelKey
import javax.inject.Singleton

@Module
abstract class WalletViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    @Singleton
    abstract fun profileViewModel(vm: WalletViewModel): ViewModel

}