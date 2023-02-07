package ru.home.swap.core.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module()
class CoreModule(val application: Application) {

    @CoreMainScope
    @Provides
    fun provideApplication(): Application {
        return application
    }

    @CoreMainScope
    @Provides
    fun provideContext(): Context {
        return application
    }
}