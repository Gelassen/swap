package ru.home.swap

import androidx.work.Configuration
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.home.swap.core.di.CoreModule
import ru.home.swap.core.di.DaggerCoreComponent
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.di.AppComponent
import ru.home.swap.di.AppModule
import ru.home.swap.di.DaggerAppComponent
import ru.home.swap.network.MyWorkerFactory
import ru.home.swap.wallet.WalletApplication
import javax.inject.Inject

class AppApplication: WalletApplication(), Configuration.Provider, HasAndroidInjector {

    @Inject
    lateinit var androidInjector : DispatchingAndroidInjector<Any>

    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    protected lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        val coreComponent = DaggerCoreComponent
            .builder()
            .coreModule(CoreModule(this))
            .networkModule(NetworkModule(this))
            .build()

        component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .coreComponent(coreComponent)
            .walletComponent(
                walletComponent
/*                DaggerWalletComponent
                    .builder()
                    .walletModule(WalletModule(this))
                    .coreComponent(coreComponent)
                    .build()*/
            )
            .build()
        component.inject(this)
    }

    @JvmName("getComponent1")
    fun getComponent(): AppComponent {
        return component
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(myWorkerFactory)
            .build()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}