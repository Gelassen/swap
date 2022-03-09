package ru.home.swap

import android.app.Application
import ru.home.swap.di.AppComponent
import ru.home.swap.di.DaggerAppComponent

class AppApplication: Application() {

    protected lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent
            .builder()
//            .appModule(AppModule(baseContext))
//            .repositoryModule(RepositoryModule(this))
            .build()
    }

    @JvmName("getComponent1")
    fun getComponent(): AppComponent {
        return component
    }
}