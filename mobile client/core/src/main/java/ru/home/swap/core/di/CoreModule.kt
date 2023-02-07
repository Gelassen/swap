package ru.home.swap.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(/*includes = [NetworkModule::class]*/)
class CoreModule(val context: Context) {

    @CoreMainScope
    @Provides
    fun provideApplication(): Context {
        return context.applicationContext
    }
}