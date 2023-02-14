package ru.home.swap.ui.profile

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.PersonRepository.*
import javax.inject.Inject

@Deprecated("Use ProfileV2ViewModel instead.")
data class Model(
    var profile: PersonProfile = PersonProfile(),
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
    val status: StateFlag = StateFlag.NONE

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (profile != other.profile) return false
        if (isLoading != other.isLoading) return false
        if (errors != other.errors) return false
        if (status != other.status) return false

        return false
    }

    override fun hashCode(): Int {
        var result = profile.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + errors.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}

@Deprecated("Use StateFlagV2 version instead")
enum class StateFlag {
    CREDENTIALS,
    PROFILE,
    NONE
}

@Deprecated("Use ProfileV2ViewModel instead. It supports on-chain operations.")
class ProfileViewModel
@Inject constructor(
    private val repository: PersonRepository,
    private val app: Application/*,
    val walletRepository: IWalletRepository,
    val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO*/
): AndroidViewModel(app)  {

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
            repository.addOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                newService = newService)
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccountAsFlow(it.data)
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
            repository.removeOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccountAsFlow(it.data)
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
            repository.addDemand(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                newService = newService)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccountAsFlow(it.data)
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
            repository.removeDemand(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccountAsFlow(it.data)
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
        Log.d(App.TAG, "[check] contact value (${uiState.value.profile.contact}) and secret value(${uiState.value.profile.secret})")
        if (PersonProvider().isAnyOfCredentialsEmpty(uiState.value.profile.contact, uiState.value.profile.secret)) {
            state.update { state ->
                state.copy(
                    errors = state.errors + app.getString(R.string.empty_credentials_error)
                )
            }
        } else {
            createAnAccount(state.value.profile)
        }
    }

    fun createAnAccount(person: PersonProfile) {
        Log.d(App.TAG, "[1] create account call (cache)")
        viewModelScope.launch {
            repository.cacheAccountAsFlow(person)
                .flatMapConcat { it->
                    Log.d(App.TAG, "[2] create account call (server)")
                    repository.createAccountAsFlow(person)
                }
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        repository.cacheAccountAsFlow(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
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
                        repository.cacheAccountAsFlow(it.data)
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

    private fun updateStateProfile(it: Response<PersonProfile>) {
        when (it) {
            is Response.Data -> {
                Log.d(App.TAG, "[5a] collect the data")
                state.update { state ->
                    state.copy(
                        profile = it.data,
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
                        profile = it.data,
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

    private fun getErrorMessage(errorResponse: Response.Error): String {
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
                state.value.profile.offers = response.data.offers
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        profile = state.profile
                    )
                }
            }
            is Response.Error -> {
                Log.d(App.TAG, "[add offer] error case")
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        errors = state.errors + getErrorMessage(response)
                    )
                }
            }
        }
    }

    private fun processAddDemandResponse(response: Response<PersonProfile>) {
        when(response) {
            is Response.Data -> {
                state.value.profile.demands = response.data.demands.toMutableList()
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        profile = state.profile
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
                state.value.profile.offers = response.data.offers.toMutableList()
                state.update { state ->
                    state.copy(
                        profile = state.profile,
                        isLoading = false

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
                state.value.profile.demands = response.data.demands.toMutableList()
                state.update { state ->
                    state.copy(
                        isLoading = false,
                        profile = state.profile
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