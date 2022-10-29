package ru.home.swap.di;


import androidx.lifecycle.ViewModel;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import ru.home.swap.core.di.ViewModelKey;
import ru.home.swap.ui.contacts.ContactsViewModel;
import ru.home.swap.ui.demands.DemandsViewModel;
import ru.home.swap.ui.offers.OffersViewModel;
import ru.home.swap.ui.profile.ProfileViewModel;

@Module/*(includes = [CoreViewModelModule::class.java])*/
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    @Singleton
    abstract ViewModel profileViewModel(ProfileViewModel vm);

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

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel.class)
    abstract ViewModel contactsViewModel(ContactsViewModel vm);

}

