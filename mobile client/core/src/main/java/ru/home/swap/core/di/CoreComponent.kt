package ru.home.swap.core.di

import android.app.Application
import android.content.Context
import dagger.Component
import ru.home.swap.core.network.IApi
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class CoreMainScope

@CoreMainScope
@Component(
    dependencies = [],
    modules = [CoreModule::class, CoreViewModelModule::class, NetworkModule::class]
)
interface CoreComponent {
    fun providesAPI(): IApi
    fun providesApplication(): Application
    fun providesContext(): Context
}