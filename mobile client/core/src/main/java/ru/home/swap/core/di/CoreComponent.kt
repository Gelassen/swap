package ru.home.swap.core.di

import dagger.Component

@Component(
    dependencies = [],
    modules = [CoreModule::class, CoreViewModelModule::class]
)
interface CoreComponent {

//    @Component.Factory
//    interface Factory {
        // Takes an instance of AppComponent when creating
        // an instance of LoginComponent
//        fun create(appComponent: AppComponent): CoreComponent
//    }

}