package ru.home.swap.ui.profile

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.PersonRepository.*
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.RegisterUserTransaction
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.TxStatus
import javax.inject.Inject
import javax.inject.Named

data class ModelV2(
    var profile: PersonProfile = PersonProfile(),
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
    val status: StateFlagV2 = StateFlagV2.NONE
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelV2

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

enum class StateFlagV2 {
    CREDENTIALS,
    PROFILE,
    NONE
}

class ProfileV2ViewModel
@Inject constructor(
    private val repository: PersonRepository,
    private val app: Application,
    val walletRepository: IWalletRepository,
    val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AndroidViewModel(app)  {

    init {
        Log.d(App.TAG, "ProfileViewModel::init call")
    }

    /*private*/ val state: MutableStateFlow<ModelV2> = MutableStateFlow(ModelV2())
    val uiState: StateFlow<ModelV2> = state
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
        val validator = PersonProvider()
        val profile = uiState.value.profile
        if (validator.isAnyOfCredentialsEmpty(profile.contact, profile.secret)
                || !validator.inputIsEthereumAddress(profile.userWalletAddress)) {
            val errorMsg = if (!validator.inputIsEthereumAddress(profile.userWalletAddress)) {
                app.getString(R.string.bad_eth_address_error)
            } else {
                app.getString(R.string.empty_credentials_error)
            }
            state.update { state ->
                state.copy(
                    errors = state.errors + errorMsg
                )
            }
        } else {
            createAnAccount(state.value.profile)
        }
    }

    fun createAnAccount(person: PersonProfile) {
        viewModelScope.launch(backgroundDispatcher) {
            try {
                var cachedPersonProfile = repository.cacheAccount(person)
                var createAccountResponse = repository.createAccount(person)
                when(createAccountResponse) {
                    is Response.Data -> {
                        repository.cacheAccount(createAccountResponse.data)
                        val cachedTx = cacheRepository.createChainTx(RegisterUserTransaction(userWalletAddress = person.userWalletAddress))
                        val chainTx = walletRepository.registerUserOnSwapMarket(userWalletAddress = person.userWalletAddress)
                        when(chainTx) {
                            is ru.home.swap.core.network.Response.Data -> {
                                if (chainTx.data.isStatusOK()) {
                                    cachedTx.status = TxStatus.TX_MINED
                                } else {
                                    cachedTx.status = TxStatus.TX_REVERTED
                                }
                            }
                            is ru.home.swap.core.network.Response.Error.Message -> {
                                cachedTx.status = TxStatus.TX_EXCEPTION
                            }
                            is ru.home.swap.core.network.Response.Error.Exception -> {
                                cachedTx.status = TxStatus.TX_EXCEPTION
                            }
                        }
                        cacheRepository.createChainTx(cachedTx)
                        withContext(Dispatchers.Main) {
                            state.update { state ->
                                if (cachedTx.status == TxStatus.TX_MINED) {
                                    state.copy(
                                        isLoading = false,
                                        profile = createAccountResponse.data,
                                        status = StateFlagV2.PROFILE
                                    )
                                } else {
                                    val txError = "Failed register the profile on chain with status ${TxStatus.TX_MINED}"
                                    state.copy(
                                        isLoading = false,
                                        errors = state.errors + txError
                                    )
                                }
                            }
                        }
                    }
                    else -> { updateStateProfile(createAccountResponse) }
                }
            } catch (ex: Exception) {
                withContext(Dispatchers.Main) {
                    state.update { state ->
                        val errors = state.errors + getErrorMessage(PersonRepository.Response.Error.Exception(ex))
                        state.copy(errors = errors, isLoading = false)
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
                        status = StateFlagV2.PROFILE,
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
                        status = if (it.data.contact.isEmpty() && it.data.secret.isEmpty()) StateFlagV2.CREDENTIALS else StateFlagV2.PROFILE,
                        isLoading = false
                    )
                }
            }
            is Response.Error -> {
                Log.d(App.TAG, "[c2] Get the result as an error ${getErrorMessage(it)}")
                state.update { state ->
                    state.copy(
                        errors = state.errors + getErrorMessage(it),
                        status = StateFlagV2.NONE,
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

    private fun preProcessResponse(it: ru.home.swap.core.network.Response<TransactionReceiptDomain>, newTx: ITransaction) {
        when(it) {
            is ru.home.swap.core.network.Response.Data -> {
                if (it.data.isStatusOK()) {
                    newTx.status = TxStatus.TX_MINED
                    cacheRepository.createChainTxAsFlow(newTx)
                } else {
                    newTx.status = TxStatus.TX_REVERTED
                    cacheRepository.createChainTxAsFlow(newTx)
                }
            }
            is ru.home.swap.core.network.Response.Error.Message -> {
                newTx.status = TxStatus.TX_EXCEPTION
                cacheRepository.createChainTxAsFlow(newTx)
            }
            is ru.home.swap.core.network.Response.Error.Exception -> {
                newTx.status = TxStatus.TX_EXCEPTION
                cacheRepository.createChainTxAsFlow(newTx)
            }
        }
    }
}