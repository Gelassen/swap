package ru.home.swap.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import ru.home.swap.R
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.network.IApi
import ru.home.swap.network.MyWorkerFactory
import ru.home.swap.repository.Cache
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.pagination.DemandsPagingSource
import ru.home.swap.repository.pagination.OffersPagingSource
import ru.home.swap.wallet.di.WalletMainScope
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.repository.TxDataSource
import ru.home.swap.wallet.storage.ChainTransactionDao
import javax.inject.Named

@Module()
class AppModule(val application: Application) {

    @AppMainScope
    @Provides
    fun provideDemandsPagingSource(api: IApi, context: Context): DemandsPagingSource {
        return DemandsPagingSource(api, Integer.parseInt(context.getString(R.string.page_size)))
    }

    @AppMainScope
    @Provides
    fun provideOffersPagingSource(api: IApi, context: Context): OffersPagingSource {
        return OffersPagingSource(api, Integer.parseInt(context.getString(R.string.page_size)))
    }

    @AppMainScope
    @Provides
    fun provideCache(): Cache {
        return Cache(application)
    }

    @AppMainScope
    @Provides
    fun providePersonRepository(api: IApi, cache: Cache, context: Context): IPersonRepository {
        return PersonRepository(api, cache, context)
    }

    @AppMainScope
    @Provides
    fun provideDataSource(dao: ChainTransactionDao, context: Context): TxDataSource {
        return TxDataSource(application, dao, Integer.parseInt(context.getString(R.string.page_size)))
    }

    @AppMainScope
    @Provides
    fun providesMyWorkerFactory(repository: IWalletRepository,
                                storageRepository: IStorageRepository,
                                @Named(NetworkModule.DISPATCHER_IO) dispatcher: CoroutineDispatcher
    ): MyWorkerFactory {
        return MyWorkerFactory(repository, storageRepository, dispatcher)
    }
}