package ru.home.swap.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.home.swap.R
import ru.home.swap.network.IApi
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.pagination.DemandsPagingSource
import ru.home.swap.repository.pagination.OffersPagingSource
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class AppModule(val context: Context) {

//    @Singleton
    @Provides
    fun provideDemandsPagingSource(api: IApi, context: Context): DemandsPagingSource {
        return DemandsPagingSource(api, Integer.parseInt(context.getString(R.string.page_size)))
    }

//    @Singleton
    @Provides
    fun provideOffersPagingSource(api: IApi, context: Context): OffersPagingSource {
        return OffersPagingSource(api, Integer.parseInt(context.getString(R.string.page_size)))
    }

//    @Singleton
    @Provides
    fun provideCache(): Cache {
        return Cache(context)
    }

//    @Singleton
    @Provides
    fun providePersonRepository(api: IApi, cache: Cache, context: Context): PersonRepository {
        return PersonRepository(api, cache, context)
    }

    @Singleton
    @Provides
    fun provideApplication(): Context {
        return context.applicationContext
    }
}