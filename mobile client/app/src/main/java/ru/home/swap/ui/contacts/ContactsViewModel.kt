package ru.home.swap.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import ru.home.swap.model.PersonProfile
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import javax.inject.Inject

data class Model(
    var ownerProfile: PersonProfile? = null,
    var counterpartyProfile: PersonProfile? = null,
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList()
)

class ContactsViewModel
    @Inject constructor(
        private val repository: PersonRepository,
        private val cache: Cache
    ): ViewModel() {

    private val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    suspend fun fetchContacts(serviceId: Long) {
        if (state.value.ownerProfile == null) {
            cache.getProfile()
                .onStart {state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat { it ->
                    state.update { state -> state.copy(ownerProfile = it) }
                    repository.getContacts(
                        contact = state.value.ownerProfile!!.contact,
                        secret = state.value.ownerProfile!!.secret,
                        serviceId = serviceId
                    )
                }
                .collect { it ->
                    processResponse(it)
                }
        } else {
            repository.getContacts(
                contact = state.value.ownerProfile!!.contact,
                secret = state.value.ownerProfile!!.secret,
                serviceId = serviceId
            )
                .onStart {state.update { state -> state.copy(isLoading = true) } }
                .collect { it ->
                    processResponse(it)
                }
        }
    }

    private fun processResponse(response: PersonRepository.Response<PersonProfile>) {
        when (response) {
            is PersonRepository.Response.Data -> {
                state.update { state ->
                    state.copy(
                        counterpartyProfile = response.data,
                        isLoading = false)
                }
            }
            is PersonRepository.Response.Error -> {
                val msg = getErrorMessage(response)
                state.update { state -> state.copy(errors = state.errors.plus(msg), isLoading = false) }
            }
        }
    }

    private fun getErrorMessage(errorResponse: PersonRepository.Response.Error): String {
        var errorMessage = ""
        if (errorResponse is PersonRepository.Response.Error.Exception) {
            errorMessage = "Something went wrong"
        } else {
            errorMessage = "Something went wrong with server response: \n\n${(errorResponse as PersonRepository.Response.Error.Message).msg}"
        }
        return errorMessage
    }

    fun removeShownError() {
        state.update { it ->
            it.copy(
                errors = it.errors.filter { str -> !str.equals(it.errors.first()) }
            )
        }
    }

}