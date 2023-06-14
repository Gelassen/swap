package ru.home.swap

import androidx.work.Configuration
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.core.di.CoreModule
import ru.home.swap.core.di.DaggerCoreComponent
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.di.AppComponent
import ru.home.swap.di.AppModule
import ru.home.swap.di.DaggerAppComponent
import ru.home.swap.network.MyWorkerFactory
import ru.home.swap.wallet.WalletApplication
import javax.inject.Inject

open class AppApplication(): WalletApplication(), Configuration.Provider, HasAndroidInjector {

    @Inject
    lateinit var androidInjector : DispatchingAndroidInjector<Any>

    @Inject
    lateinit var myWorkerFactory: MyWorkerFactory

    protected lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        // TODO: if a test scenario DI config would be done, DI configuration should be move into separate class
        component = prepareAppComponent()
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

    protected open fun prepareAppComponent(): AppComponent {
        return DaggerAppComponent
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
    }

    fun setDependencyComponent(component: AppComponent) {
        this.component = component
    }
}