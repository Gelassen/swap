package ru.home.swap.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.home.swap.network.IApi
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class AppModule(val context: Context) {

    @Singleton
    @Provides
    fun provideCache(): Cache {
        return Cache(context)
    }

    @Singleton
    @Provides
    fun providePersonRepository(api: IApi, cache: Cache): PersonRepository {
        return PersonRepository(api, cache)
    }
}