package ru.home.swap

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.home.swap.core.R
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.core.di.CoreModule
import ru.home.swap.core.di.DaggerCoreComponent
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.network.IApi
import ru.home.swap.di.AppComponent
import ru.home.swap.di.AppModule
import ru.home.swap.di.DaggerAppComponent
import ru.home.swap.wallet.di.DaggerWalletComponent
import ru.home.swap.wallet.di.WalletComponent
import ru.home.swap.wallet.di.WalletModule

class TestAppApplication: AppApplication() {

/*    fun getTestComponent(): AppComponent {
        return DaggerAppComponent
            .builder()
            .appModule(AppModule(application))
            .coreComponent(prepareCoreComponent())
            .walletComponent(prepareWalletComponent())
            .build()
    }

    private fun prepareWalletComponent(): WalletComponent {
        return DaggerWalletComponent
            .builder()
            .walletModule(WalletModule(application))
            .coreComponent(prepareCoreComponent())
            .build()
    }

    private fun prepareCoreComponent(): CoreComponent {
        return DaggerCoreComponent
            .builder()
            .coreModule(CoreModule(application))
            .networkModule(TestNetworkModule(application))
            .build()
    }*/

    override fun prepareAppComponent(): AppComponent {
        return DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .coreComponent(coreComponent)
            .walletComponent(walletComponent)
            .build()
    }

    override fun prepareWalletComponent(): WalletComponent {
        return super.prepareWalletComponent()
    }

    override fun prepareCoreComponent(): CoreComponent {
        return DaggerCoreComponent
            .builder()
            .coreModule(CoreModule(this))
            .networkModule(TestNetworkModule(this))
            .build()
    }

    private class TestNetworkModule(context: Context) : NetworkModule(context) {

        override fun providesApi(httpClient: OkHttpClient): IApi {
            val url = "http://10.0.3.2:3100"//context.getString(R.string.url)
            val retrofit = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create(customGson))
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(url)
                .build()

            return retrofit.create(IApi::class.java)
//            return super.providesApi(httpClient)
        }
    }
}