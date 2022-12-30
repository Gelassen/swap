package ru.home.swap

import android.app.Application
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.di.AppComponent
import ru.home.swap.di.AppModule
import ru.home.swap.di.DaggerAppComponent
import javax.inject.Inject

class AppApplication: Application(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector : DispatchingAndroidInjector<Any>

    protected lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .networkModule(NetworkModule(this))
            .build()
        component.inject(this)
    }

    @JvmName("getComponent1")
    fun getComponent(): AppComponent {
        return component
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}