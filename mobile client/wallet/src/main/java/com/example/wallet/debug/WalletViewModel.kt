package com.example.wallet.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Model(
    var privateKey: String = "", // TODO refactor this dev mode solution
    val errors: List<String> = emptyList()
)
class WalletViewModel(): ViewModel() {

    private val repository: WalletRepository = WalletRepository()

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun mintToken() {
        viewModelScope.launch {
            repository.mintToken()
                .collect {
                    // TODO complete me
                }
        }
    }
}