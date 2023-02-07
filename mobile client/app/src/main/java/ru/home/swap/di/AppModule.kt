package ru.home.swap.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import ru.home.swap.R
import ru.home.swap.core.network.IApi
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.pagination.DemandsPagingSource
import ru.home.swap.repository.pagination.OffersPagingSource

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
    fun providePersonRepository(api: IApi, cache: Cache, context: Context): PersonRepository {
        return PersonRepository(api, cache, context)
    }
}