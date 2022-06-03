package ru.home.swap.ui.profile

import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.network.model.EmptyPayload
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.PersonRepository.*
import javax.inject.Inject

// TODO refactor model to with profile object and remove redundant extra classes
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
    var id: Long? = null,
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
        val newService = Service(title = proposal.get()!!, date = 0L, index = listOf())
        viewModelScope.launch {
            repository.addOffer(uiState.value.contact, uiState.value.secret, newService)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccount(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it ->
                    Log.d(App.TAG, "[add offer] start collect data in viewmodel")
                    processAddOfferResponse(it)
                }
        }
    }

    fun removeOffer(item: Service) {
        viewModelScope.launch {
            repository.removeOffer(uiState.value.contact, uiState.value.secret, item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccount(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it ->
                    processRemoveOfferResponse(it)
                }
        }
    }

    fun addDemand() {
        val newService = Service(title = proposal.get()!!, date = 0L, index = listOf())
        viewModelScope.launch {
            repository.addDemand(uiState.value.contact, uiState.value.secret, newService)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccount(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it ->
                    processAddDemandResponse(it)
                }
        }
    }

    fun removeDemand(item: Service) {
        viewModelScope.launch {
            repository.removeDemand(uiState.value.contact, uiState.value.secret, item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccount(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it ->
                    processRemoveDemandsResponse(it)
                }
        }
    }

    fun createAnAccount() {
        Log.d(App.TAG, "[check] contact value (${state.value.contact}) and secret value(${state.value.secret})")
        if (PersonProvider().isAnyOfCredentialsEmpty(state.value.contact, state.value.secret)) {
            state.update { state ->
                state.copy(
                    errors = state.errors + application.getString(R.string.empty_credentials_error)
                )
            }
        } else {
            createAnAccount(
                PersonProfile(
                    id = state.value.id,
                    contact = state.value.contact,
                    secret = state.value.secret,
                    name = state.value.name,
                    offers = state.value.offers,
                    demands = state.value.demands
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
                    updateStateProfile(it)
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
                        flow<Response<PersonProfile>> {
                            emit(Response.Data(it))
                        }.stateIn(viewModelScope)
                    } else {
                        Log.d(App.TAG, "[b2] Get cached account call and request server for an actual one")
                        repository.getAccount(it)
                    }
                }
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccount(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
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
                    updateState(it)
                }
        }
    }

    private fun updateStateProfile(it: Response<EmptyPayload>) {
        when (it) {
            is Response.Data -> {
                Log.d(App.TAG, "[5a] collect the data")
                state.update { state ->
                    state.copy(
                        status = StateFlag.PROFILE,
                        isLoading = false
                    )
                }
            }
            is Response.Error -> {
                Log.d(App.TAG, "[5b] collect the error")
                state.update { state ->
                    state.copy(
                        errors = state.errors + getErrorMessage(it),
                        isLoading = false
                    )
                }
                viewModelScope.launch {
                    repository.cleanCachedAccount()
                        .collect { it ->
                            /* no op */
                            Log.d(App.TAG, "Receive callback from cache clean")
                        }
                }
            }
        }
    }

    private fun updateState(it: Response<PersonProfile>) {
        when (it) {
            is Response.Data -> {
                Log.d(App.TAG, "[c1] Get the result as a data")
                val offers = mutableListOf<Service>()
                offers.addAll(it.data.offers)
                val demands = mutableListOf<Service>()
                demands.addAll(it.data.demands)
                Log.d(App.TAG, "[offers] ${offers.count()}")
                Log.d(App.TAG, "[offers] ${offers.toString()}")
                state.update { state ->
                    state.copy(
                        contact = it.data.contact,
                        secret = it.data.secret,
                        name = it.data.name,
                        offers = offers,
                        demands = demands,
                        status = if (it.data.contact.isEmpty() && it.data.secret.isEmpty()) StateFlag.CREDENTIALS else StateFlag.PROFILE,
                        isLoading = false
                    )
                }
            }
            is Response.Error -> {
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

    protected fun getErrorMessage(errorResponse: Response.Error): String {
        var errorMessage = ""
        if (errorResponse is Response.Error.Exception) {
            errorMessage = "Something went wrong"
        } else {
            errorMessage = "Something went wrong with server response: \n\n${(errorResponse as Response.Error.Message).msg}"
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

    private fun processAddOfferResponse(response: Response<PersonProfile>) {
        when(response) {
            is Response.Data<PersonProfile> -> {
                Log.d(App.TAG, "[add offer] positive case")
                state.update { state ->
                    state.copy(
                        offers = response.data.offers.toMutableList()
                    )
                }
            }
            is Response.Error -> {
                Log.d(App.TAG, "[add offer] error case")
                state.update { state ->
                    state.copy(
                        errors = state.errors + getErrorMessage(response)
                    )
                }
            }
        }
    }

    private fun processAddDemandResponse(response: Response<PersonProfile>) {
        when(response) {
            is Response.Data -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        demands = response.data.demands.toMutableList()
                    )
                }
            }
            is Response.Error -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        errors = state.errors + getErrorMessage(response)
                    )
                }
            }
        }
    }

    private fun processRemoveOfferResponse(response: Response<PersonProfile>) {
        when(response) {
            is Response.Data -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        offers = response.data.offers.toMutableList()
                    )
                }
            }
            is Response.Error -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        errors = state.errors + getErrorMessage(response)
                    )
                }
            }
        }
    }

    private fun processRemoveDemandsResponse(response: Response<PersonProfile>) {
        when(response) {
            is Response.Data -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        demands = response.data.demands.toMutableList()
                    )
                }
            }
            is Response.Error -> {
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        errors = state.errors + getErrorMessage(response)
                    )
                }
            }
        }
    }
}