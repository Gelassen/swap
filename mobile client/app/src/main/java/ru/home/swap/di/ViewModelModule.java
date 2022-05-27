package ru.home.swap.di;


import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import ru.home.swap.ui.demands.DemandsViewModel;
import ru.home.swap.ui.offers.OffersViewModel;
import ru.home.swap.ui.profile.ProfileViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory viewModelFactory);
    //You are able to declare ViewModelProvider.Factory dependency in another module. For example in ApplicationModule.

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    @Singleton
    abstract ViewModel profileViewModel(ProfileViewModel vm);

    //Others ViewModels

    @Binds
    @IntoMap
    @ViewModelKey(OffersViewModel.class)
    @Singleton
    abstract ViewModel offersViewModel(OffersViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(DemandsViewModel.class)
    @Singleton
    abstract ViewModel demandsViewModel(DemandsViewModel vm);

}

