package ru.home.swap.di;


import androidx.lifecycle.ViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import ru.home.swap.core.di.ViewModelKey;
import ru.home.swap.ui.chains.ChainsViewModel;
import ru.home.swap.ui.contacts.ContactsViewModel;
import ru.home.swap.ui.demands.DemandsViewModel;
import ru.home.swap.ui.offers.OffersViewModel;
import ru.home.swap.ui.profile.ProfileV2ViewModel;
import ru.home.swap.ui.profile.ProfileViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    @AppMainScope
    abstract ViewModel profileViewModel(ProfileViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ProfileV2ViewModel.class)
    @AppMainScope
    abstract ViewModel profileV2ViewModel(ProfileV2ViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(OffersViewModel.class)
    @AppMainScope
    abstract ViewModel offersViewModel(OffersViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(DemandsViewModel.class)
    @AppMainScope
    abstract ViewModel demandsViewModel(DemandsViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ContactsViewModel.class)
    @AppMainScope
    abstract ViewModel contactsViewModel(ContactsViewModel vm);

    @Binds
    @IntoMap
    @ViewModelKey(ChainsViewModel.class)
    @AppMainScope
    abstract ViewModel chainsViewModel(ChainsViewModel vm);

}

