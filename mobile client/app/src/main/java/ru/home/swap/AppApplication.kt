package ru.home.swap

import android.app.Application
import ru.home.swap.di.AppComponent
import ru.home.swap.di.AppModule
import ru.home.swap.di.DaggerAppComponent
import ru.home.swap.di.NetworkModule

class AppApplication: Application() {

    protected lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .networkModule(NetworkModule(this))
            .build()
    }

    @JvmName("getComponent1")
    fun getComponent(): AppComponent {
        return component
    }
}