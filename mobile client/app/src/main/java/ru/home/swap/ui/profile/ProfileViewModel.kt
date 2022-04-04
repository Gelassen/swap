package ru.home.swap.ui.profile

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.PersonRepository.*
import javax.inject.Inject

sealed interface IModel {
    val isLoading: Boolean
    val errors: List<String>
    val status: StateFlag

    data class ProfileState(
        var name: String = "",
        val offers: MutableList<Service> = mutableListOf<Service>(),
        val demands: MutableList<Service> = mutableListOf<Service>(),
        override val isLoading: Boolean = false,
        override val errors: List<String> = emptyList(),
        override val status: StateFlag = StateFlag.PROFILE
    ) : IModel {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ProfileState

            if (name != other.name) return false
            if (offers != other.offers) return false
            if (demands != other.demands) return false
            if (isLoading != other.isLoading) return false
            if (errors != other.errors) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + offers.hashCode()
            result = 31 * result + demands.hashCode()
            result = 31 * result + isLoading.hashCode()
            result = 31 * result + errors.hashCode()
            result = 31 * result + status.hashCode()
            return result
        }
    }

    data class CredentialsState(
        var contact: String,
        var secret: String,
        override val isLoading: Boolean = false,
        override val errors: List<String> = emptyList(),
        override val status: StateFlag = StateFlag.CREDENTIALS
    ) : IModel

    data class NoState(
        override val isLoading: Boolean,
        override val errors: List<String>,
        override val status: StateFlag = StateFlag.NONE

    ) : IModel
}
data class Model(
    var contact: String = "",
    var secret: String = "",
    var name: String = "",
    val offers: MutableList<Service> = mutableListOf<Service>(),
    val demands: MutableList<Service> = mutableListOf<Service>(),
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
    val status: StateFlag = StateFlag.NONE
) {
    fun toUiState(): IModel {
        when(status) {
            StateFlag.CREDENTIALS -> {
                return IModel.CredentialsState(
                    contact = contact,
                    secret = secret,
                    isLoading = isLoading,
                    errors = errors
                )
            }
            StateFlag.PROFILE -> {
                return IModel.ProfileState(
                    name = name,
                    offers = offers,
                    demands = demands,
                    isLoading = isLoading,
                    errors = errors
                )
            }
            StateFlag.NONE -> {
                return IModel.NoState(
                    isLoading = isLoading,
                    errors = errors
                )
            }
        }

    }
}

enum class StateFlag {
    CREDENTIALS,
    PROFILE,
    NONE
}

class ProfileViewModel
@Inject constructor(
    private val repository: PersonRepository,
    private val application: Context
): ViewModel()  {

    init {
        Log.d(App.TAG, "ProfileViewModel::init call")
    }

    /*private*/ val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    val proposal: ObservableField<String> = ObservableField<String>("")

    fun addItem(isOfferSelected: Boolean) {
        if (proposal.get()!!.isEmpty()) return
        if (isOfferSelected) {
            addOffer()
        } else {
            addDemand()
        }
    }

    fun addOffer() {
        val newService = Service(proposal.get()!!, 0L, listOf())
        viewModelScope.launch {
            repository.addOffer(uiState.value.contact, uiState.value.secret, newService)
                .collect { it ->
                    when(it) {
                        is Response.Data<PersonProfile> -> {
                            state.update { state ->
                                state.copy(
                                    offers = it.data.person.offers.toMutableList()
                                )
                            }
                        }
                        is Response.Error -> {
                            state.update { state ->
                                state.copy(
                                    errors = state.errors + getErrorMessage(it)
                                )
                            }
                        }
                    }
                }
        }
/*        state.update { state ->
            val updatedOffers = mutableListOf<Service>()
            updatedOffers.addAll(state.offers)
            updatedOffers.add(newService)
            Log.d(App.TAG, "[items] add new service")
            proposal.set("")
            state.copy(offers = updatedOffers)
        }
        Log.d(App.TAG, "[items] add offer end ${state.value.offers}")*/
    }

    fun removeOffer(item: Service) {
        state.update { state ->
            val updatedOffers = mutableListOf<Service>()
            updatedOffers.addAll(state.offers)
            updatedOffers.remove(item)
            Log.d(App.TAG, "[state] offers after removed item ${updatedOffers.count()}")
            Log.d(App.TAG, "[items] remove offer call")
            state.copy(offers = updatedOffers)
        }
        Log.d(App.TAG, "[state] end (${state.value.offers})")
        Log.d(App.TAG, "[items] remove offer end call ${state.value.offers.count()}")
    }

    fun addDemand() {
        val newService = Service(proposal.get()!!, 0L, listOf())
        state.update { state ->
            val updatedDemands = mutableListOf<Service>()
            updatedDemands.addAll(state.demands)
            updatedDemands.add(newService)
            proposal.set("")
            state.copy(demands = updatedDemands)
        }
    }

    fun removeDemand(item: Service) {
        state.update { state ->
            val updatedDemands = mutableListOf<Service>()
            updatedDemands.addAll(state.demands)
            updatedDemands.remove(item)
            state.copy(demands = updatedDemands)
        }
    }

    fun createAnAccount() {
        Log.d(App.TAG, "[check] contact value (${state.value.contact}) and secret value(${state.value.secret})")
        if (PersonProvider().isCredentialsEmpty(state.value.contact, state.value.secret)) {
            state.update { state ->
                state.copy(
                    errors = state.errors + application.getString(R.string.empty_credentials_error)
                )
            }
        } else {
            createAnAccount(
                PersonProfile(
                    contact = state.value.contact,
                    secret = state.value.secret,
                    person = Person()
                )
            )
        }
    }

    fun createAnAccount(person: PersonProfile) {
        Log.d(App.TAG, "[1] create account call (cache)")
        viewModelScope.launch {
            repository.cacheAccount(person)
                .flatMapConcat { it->
                    Log.d(App.TAG, "[2] create account call (server)")
                    repository.createAccount(person)
                }
                .catch { e ->
                    Log.d(App.TAG, "[3] get an exception in catch block")
                    Log.e(App.TAG, "Got an exception during network call", e)
                    state.update { state ->
                        val errors = state.errors + getErrorMessage(PersonRepository.Response.Error.Exception(e))
                        state.copy(errors = errors, isLoading = false)
                    }
                }
                .collect { it ->
                    Log.d(App.TAG, "[4] collect the result")
                    when (it) {
                        is PersonRepository.Response.Data -> {
                            Log.d(App.TAG, "[5a] collect the data")
                            state.update { state ->
                                state.copy(
                                    contact = person.contact,
                                    secret = person.secret,
                                    name = person.person.name,
                                    status = StateFlag.PROFILE,
                                    isLoading = false
                                )
                            }
                        }
                        is PersonRepository.Response.Error -> {
                            Log.d(App.TAG, "[5b] collect the error")
                            state.update { state ->
                                state.copy(
                                    errors = state.errors + getErrorMessage(it),
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    fun checkAnExistingAccount() {
        viewModelScope.launch {
            Log.d(App.TAG, "[a] Get cached account call")
            repository.getCachedAccount()
                .flatMapConcat { it ->
                    if (it.contact.isEmpty() && it.secret.isEmpty()) {
                        Log.d(App.TAG, "[b1] Get cached account as an empty")
                        flow<PersonRepository.Response<PersonProfile>> {
                            emit(PersonRepository.Response.Data(it))
                        }.stateIn(viewModelScope)
                    } else {
                        Log.d(App.TAG, "[b2] Get cached account call and request server for an actual one")
                        repository.getAccount(it)
                    }
                }
                .onStart {
                    state.update { state ->
                        state.copy(
                            isLoading = true
                        )
                    }
                }
                .catch { e ->
                    Log.e(App.TAG, "Get an error when obtaining account", e)
                    state.update { state ->
                        val errors = state.errors + getErrorMessage(PersonRepository.Response.Error.Exception(e))
                        state.copy(errors = errors, isLoading = false)
                    }
                }
                .collect { it ->
                    Log.d(App.TAG, "[c] Get the result")
                    when (it) {
                        is PersonRepository.Response.Data -> {
                            Log.d(App.TAG, "[c1] Get the result as a data")
                            val offers = mutableListOf<Service>()
                            offers.addAll(it.data.person.offers)
                            val demands = mutableListOf<Service>()
                            demands.addAll(it.data.person.demands)
                            Log.d(App.TAG, "[offers] ${offers.count()}")
                            Log.d(App.TAG, "[offers] ${offers.toString()}")
                            state.update { state ->
                                state.copy(
                                    contact = it.data.contact,
                                    secret = it.data.secret,
                                    name = it.data.person.name,
                                    offers = offers,
                                    demands = demands,
                                    status = if (it.data.contact.isEmpty() && it.data.secret.isEmpty()) StateFlag.CREDENTIALS else StateFlag.PROFILE,
                                    isLoading = false
                                )
                            }
                        }
                        is PersonRepository.Response.Error -> {
                            Log.d(App.TAG, "[c2] Get the result as an error ${getErrorMessage(it)}")
                            state.update { state ->
                                state.copy(
                                    errors = state.errors + getErrorMessage(it),
                                    status = StateFlag.NONE,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    protected fun getErrorMessage(errorResponse: Response.Error): String {
        var errorMessage = ""
        if (errorResponse is Response.Error.Exception) {
            errorMessage = "Something went wrong"
        } else {
            errorMessage = "Something went wrong with server response: \\n ${(errorResponse as Response.Error.Message).msg}"
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