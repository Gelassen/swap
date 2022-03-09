package ru.home.swap.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.home.swap.model.PersonView
import ru.home.swap.repository.PersonRepository
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {
    // TODO: Implement the ViewModel
    private var _model: MutableStateFlow<List<PersonView>> = MutableStateFlow(Collections.emptyList())
    val model: StateFlow<List<PersonView>> = _model

    init {
        viewModelScope.launch {
            _model.value = PersonRepository().getOffers()
        }
    }

}