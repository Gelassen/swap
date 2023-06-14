package ru.home.swap.core

import android.app.Application
import ru.home.swap.core.di.CoreComponent
import ru.home.swap.core.di.CoreModule
import ru.home.swap.core.di.DaggerCoreComponent
import ru.home.swap.core.di.NetworkModule

open class CoreApplication(): Application() {

    protected lateinit var coreComponent: CoreComponent

    override fun onCreate() {
        super.onCreate()
        coreComponent = prepareCoreComponent()
    }

    protected open fun prepareCoreComponent(): CoreComponent {
        return DaggerCoreComponent
            .builder()
            .coreModule(CoreModule(this))
            .networkModule(NetworkModule(this))
            .build()
    }
}