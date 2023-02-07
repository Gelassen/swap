package ru.home.swap.core.di

import dagger.Component
import ru.home.swap.core.network.IApi
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class CoreMainScope

@CoreMainScope
@Component(
    dependencies = [],
    modules = [CoreModule::class, CoreViewModelModule::class, NetworkModule::class]
)
interface CoreComponent {

    fun network(): IApi

//    @Component.Factory
//    interface Factory {
        // Takes an instance of AppComponent when creating
        // an instance of LoginComponent
//        fun create(appComponent: AppComponent): CoreComponent
//    }

}