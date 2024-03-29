package ru.home.swap.di

import android.app.Application
import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.home.swap.AppApplication
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
import ru.home.swap.wallet.model.ChainConfig

class TestAppApplication: AppApplication() {

    companion object TestConfig {
        const val SERVER_ENDPOINT = "http://10.0.3.2:3100"
        const val ETHEREUM_ENDPOINT = "http://192.168.1.17:2004/"
        const val SWAP_TOKEN_ADDRESS = "0x699e2DE0c5EdD6Aa0d6319a6cBBbb0a902269d45"
        const val SWAP_MARKET_ADDRESS = "0x3276D551e55Ce2EfEFB71c4Fb339ad37617a1eBa"
        const val CHAIN_ID = "50102"

        const val FIRST_USER_NAME = "Dmitry"
        const val FIRST_USER_CONTACT = "+79207008090"
        const val FIRST_USER_SECRET = "onemoretime"
        const val FIRST_USER_ADDRESS = "0x04C688b38E3Ca63EC888EbEBed6d39cde0833a71"
        const val FIRST_USER_PRIVATE_KEY = "1b10d8e57de10a45bfefd9118f53be67df3e92a2ed805b9b87d22a494f1ca81a"
    }

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
        return DaggerWalletComponent
            .builder()
            .walletModule(TestWalletModule(this))
            .coreComponent(coreComponent)
            .build()
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
            val url = TestConfig.SERVER_ENDPOINT
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl(url)
                .build()

            return retrofit.create(IApi::class.java)
        }
    }

    private class TestWalletModule(context: Application): WalletModule(context) {

        override fun providesEthereumEndpoint(): String {
            return TestConfig.ETHEREUM_ENDPOINT
        }

        override fun providesChainConfig(): ChainConfig {
            return ChainConfig(
                chainId = TestConfig.CHAIN_ID.toLong(),
                swapTokenAddress = TestConfig.SWAP_TOKEN_ADDRESS,
                swapMarketAddress = TestConfig.SWAP_MARKET_ADDRESS,
                accountPrivateKey = TestConfig.FIRST_USER_PRIVATE_KEY
            )
        }
    }
}