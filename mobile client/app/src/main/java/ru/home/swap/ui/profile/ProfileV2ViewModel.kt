package ru.home.swap.ui.profile

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.extensions.registerIdlingResource
import ru.home.swap.core.extensions.unregisterIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.ChainService
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.toJson
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.PersonRepository.*
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.model.RegisterUserTransaction
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.network.ChainWorker
import ru.home.swap.wallet.network.getWorkRequest
import ru.home.swap.wallet.providers.WalletProvider
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.TxStatus
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Named

data class ModelV2(
    var profile: PersonProfile = PersonProfile(),
    var pendingTx: ConcurrentLinkedQueue<Pair<ITransaction, Service>> = ConcurrentLinkedQueue(),
    val isLoading: Boolean = false,
    val isAllowedToProcess: Boolean = true,
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

/**
 * In the current split between work manager operations and network operations
 * based on the feedback from the storage, extra (server side) data also
 * should be cached.
 * */
class ProfileV2ViewModel
@Inject constructor(
    private val app: Application,
    private val personRepository: IPersonRepository,
    private val walletRepository: IWalletRepository,
    private val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AndroidViewModel(app)  {

    init {
        Log.d(App.TAG, "ProfileViewModel::init call")
        loadAllFromCache()
    }

    private val logger = Logger.getInstance()

    private val assignedUri = "uri is turned off in current version"

    /*private*/ val state: MutableStateFlow<ModelV2> = MutableStateFlow(ModelV2())
    val uiState: StateFlow<ModelV2> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    val proposal: ObservableField<String> = ObservableField<String>("")

    fun getPersonWalletAddress() {
        viewModelScope.launch {
            personRepository
                .getCachedAccount()
                .flowOn(backgroundDispatcher)
                .collect { it ->
                    state.update { state ->
                        val profile = PersonProfile(state.profile)
                        profile.userWalletAddress = it.userWalletAddress
                        state.copy(profile = profile)
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
//    @Deprecated("Work on this method has been frozen")
    fun backgroundProcessMinedTx() {
        viewModelScope.launch {
            flow<Pair<ITransaction, Service>> {
                while (uiState.value.isAllowedToProcess) {
                    val delay = 1120L
                    // wait for user's wallet address will be obtained from the cache during initialisation
                    if (state.value.profile.userWalletAddress.isEmpty()) {
                        logger.d("[queue polling] delay() call")
                        delay(delay)
                        continue
                    }
                    if (state.value.pendingTx.isEmpty()) {
                        logger.d("[queue polling] delay() call")
                        delay(delay)
                    }
                    val item = state.value.pendingTx.poll()
                    if (item != null) {
                        logger.d("[queue polling] emit item from queue ${item.toString()}")
                        emit(item) }
                    logger.d("[queue polling] delay() call")
                    delay(delay)
                }
            }
                .flatMapConcat { item ->
                    // TODO consider to move each branch case into the separate class function
                    when(item.first.type) {
                        MintTransaction::class.java.simpleName -> {
                            /* token are created only for offer, demands registered just as a db record */
//                            val mintItem = (item.first as MintTransaction).tokenId
                            logger.d("[queue polling] process MintTransaction item")
                            item.second.chainService.tokenId = (item.first as MintTransaction).tokenId
                            // val chainService = item.second//ChainService(userWalletAddress = state.value.profile.userWalletAddress, tokenId = mintItem)
                            val newService = item.second//Service(title = proposal.get()!!, date = 0L, index = listOf(), chainService = chainService)
                            logger.d("[addOffer::server] ${item.first} \n and \n ${item.second}")
                            personRepository.addOffer(
                                contact = uiState.value.profile.contact,
                                secret = uiState.value.profile.secret,
                                newService = newService
                            )
                        }
                        else -> { throw UnsupportedOperationException("Did you forget to add support of ${item.first.type} class tx?") }
                    }
                }
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat {
                    if (it is Response.Data) { personRepository.cacheAccount(it.data) }
                    flow { emit(it) }
                }
                .collect { it ->
                    Log.d(App.TAG, "[add offer] start collect data in viewmodel")
                    processServerResponse(it) { addOfferSpecialHandler(it) }
                }

        }
    }

    fun addItem(isOfferSelected: Boolean) {
        if (proposal.get()!!.isEmpty()) return
        if (isOfferSelected) {
            addOffer()
        } else {
            addDemand()
        }
    }

    // TODO test me, nexus 7 doesn't support java 8 streams API - migrate to a new device?
    fun addOffer() {
        logger.d("[start] mintToken()")
        viewModelScope.launch {
            // TODO define entity to domain object converters for server side metadata
            // TODO pass this data within worker object
            // TODO extend worker to save this server side metadata in cache too
            // TODO extend dao to support tx entity with server side metadata

            // TODO finish migration to TxWithMetadata - the idea to check concept
            //  on a single instance and expand it later to overall tx support
            // TODO listen changes in storage to run request to the backend

            val workManager = WorkManager.getInstance(app)

            val requestBuilder = RequestBuilder()
            val work = ChainWorker.Builder.build(
                requestBuilder.prepareToAddress(),
                requestBuilder.prepareValueParam(),
                assignedUri,
                requestBuilder.prepareServiceParam()
            )
            val workRequest = workManager.getWorkRequest<ChainWorker>(work)
            workManager.enqueue(workRequest)
            workManager
                .getWorkInfoByIdLiveData(workRequest.id)
                .asFlow()
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> { logger.d("[mint token] succeeded status with result: ${it.toString()}")}
                        WorkInfo.State.FAILED -> {
                            logger.d("[mint token] failed status with result: ${it.toString()}")
                            var error = ""
                            if (it.outputData.keyValueMap.containsKey(ChainWorker.KEY_ERROR_MSG)) {
                                error = it.outputData.keyValueMap.get(ChainWorker.KEY_ERROR_MSG) as String
                            } else {
                                error = "Something went during minting the token"
                            }
                            state.update { state -> state.copy(isLoading = false, errors = state.errors.plus(error)) }
                        }
                        else -> { logger.d("[mint token] unexpected state with result: ${it.toString()}") }
                    }
                }
        }
        logger.d("[end] mintToken()")
/*        val newService = Service(title = proposal.get()!!, date = 0L, index = listOf())
        viewModelScope.launch {
            personRepository.addOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                newService = newService)
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        personRepository.cacheAccountAsFlow(it.data)
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
                    processServerResponse(it) { addOfferSpecialHandler(it) }
                }
        }*/
    }

    fun removeOffer(item: Service) {
        viewModelScope.launch {
            personRepository.removeOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        personRepository.cacheAccountAsFlow(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it -> processServerResponse(it) { removeOfferSpecialHandler(it) } }
        }
    }

    fun addDemand() {
        val newService = Service(title = proposal.get()!!, date = 0L, index = listOf())
        viewModelScope.launch {
            personRepository.addDemand(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                newService = newService)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        personRepository.cacheAccountAsFlow(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it -> processServerResponse(it) { addDemandSpecialHandler(it) } }
        }
    }

    fun removeDemand(item: Service) {
        viewModelScope.launch {
            personRepository.removeDemand(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = item.id)
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        personRepository.cacheAccountAsFlow(it.data)
                            .collect { it ->
                                // no op, just execute the command
                                Log.d(App.TAG, "account has been cached")
                            }
                    }
                    flow {
                        emit(it)
                    }
                }
                .collect { it -> processServerResponse(it) { removeDemandSpecialHandler(it) } }
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
        state.update { state -> state.copy(isLoading = true) }
        viewModelScope.launch(backgroundDispatcher) {
            try {
                app.registerIdlingResource()
                var cachedPersonProfile = this@ProfileV2ViewModel.personRepository.cacheAccount(person)
                var createAccountResponse = this@ProfileV2ViewModel.personRepository.createAccount(person)
                when(createAccountResponse) {
                    is Response.Data -> {
                        this@ProfileV2ViewModel.personRepository.cacheAccount(createAccountResponse.data)
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
                                // cover the case when user has been registered on both backend and chain, aka sign-in
                                if (chainTx.error.message?.contains("User already registered") == true) {
                                    // we consider previous successful tx as sharing its mined status with this tx
                                    // to avoid confusion from user perspective who will observe UI
                                    cachedTx.status = TxStatus.TX_MINED
                                } else {
                                    cachedTx.status = TxStatus.TX_EXCEPTION
                                }
                            }
                        }
                        cacheRepository.createChainTx(cachedTx)
                        withContext(Dispatchers.Main) {
                            state.update { state ->
                                if (cachedTx.status == TxStatus.TX_MINED
                                    || (chainTx as ru.home.swap.core.network.Response.Error.Exception).error.message?.contains("User already registered") == true) {
                                    state.copy(
                                        isLoading = false,
                                        profile = createAccountResponse.data,
                                        status = StateFlagV2.PROFILE
                                    )
                                } else {
                                    val txError = "Failed register the profile on chain with status ${cachedTx.status}"
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
            finally {
                app.unregisterIdlingResource()
            }
        }
    }

    fun checkAnExistingAccount() {
        viewModelScope.launch {
            Log.d(App.TAG, "[a] Get cached account call")
            personRepository.getCachedAccount()
                .flatMapConcat { it ->
                    if (it.contact.isEmpty() && it.secret.isEmpty()) {
                        Log.d(App.TAG, "[b1] Get cached account as an empty")
                        flow<Response<PersonProfile>> {
                            emit(Response.Data(it))
                        }.stateIn(viewModelScope)
                    } else {
                        Log.d(App.TAG, "[b2] Get cached account call and request server for an actual one")
                        personRepository.getAccount(it)
                    }
                }
                .flatMapConcat { it ->
                    if (it is Response.Data) {
                        personRepository.cacheAccountAsFlow(it.data)
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

    // FIXME it does not notify client after changes in db
    fun loadByPage(): Flow<PagingData<ITransaction>> {
        return cacheRepository
            .getChainTransactionsByPage()
            .flowOn(backgroundDispatcher)
            .cachedIn(viewModelScope)
    }

    // it successfully notifies client after changes in db
    fun loadAllFromCache() {
        viewModelScope.launch {
            cacheRepository
                .getAllChainTransactions()
                .map { it -> it.filter { it.first.status == TxStatus.TX_MINED } }
                .flowOn(backgroundDispatcher)
                .collect {
                    logger.d("[loadAllFromCache] Collect result ${it.toString()}")
                    state.update { state ->
                        val newValues = it.subtract(uiState.value.pendingTx)
                        val data = ConcurrentLinkedQueue(state.pendingTx)
                        data.addAll(newValues)
                        /*state.pendingTx.addAll(newValues)*/ // not sure if this insert changes the state
                        state.copy(pendingTx = data)
                    }
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
                    personRepository.cleanCachedAccount()
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

    private fun processServerResponse(
        response: Response<PersonProfile>,
        specialHandler: (data: Response.Data<PersonProfile>)  -> Unit) {
        when(response) {
            is Response.Data<PersonProfile> -> {
                specialHandler(response)
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

    private fun addOfferSpecialHandler(response: Response.Data<PersonProfile>) {
        Log.d(App.TAG, "[add offer] positive case")
        state.value.profile.offers = response.data.offers
        state.update { state ->
            state.copy(
                isLoading = false,
                profile = state.profile
            )
        }
    }

    private fun addDemandSpecialHandler(response: Response.Data<PersonProfile>) {
        state.value.profile.demands = response.data.demands.toMutableList()
        state.update { state ->
            state.copy(
                isLoading = false,
                profile = state.profile
            )
        }
    }

    private fun removeOfferSpecialHandler(response: Response.Data<PersonProfile>) {
        state.value.profile.offers = response.data.offers.toMutableList()
        state.update { state ->
            state.copy(
                profile = state.profile,
                isLoading = false
            )
        }
    }

    private fun removeDemandSpecialHandler(response: Response.Data<PersonProfile>) {
        state.value.profile.demands = response.data.demands.toMutableList()
        state.update { state ->
            state.copy(
                isLoading = false,
                profile = state.profile
            )
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

    inner class RequestBuilder {

        fun prepareToAddress(): String {
            return uiState.value.profile.userWalletAddress
        }

        fun prepareValueParam(): String {
            val walletProvider = WalletProvider()
            val valueAsJson = walletProvider.getValueAsJson(
                Value(
                    offer = proposal.get()!!,
                    availableSince = walletProvider.getAYearAfter()
                )
            )
            return valueAsJson
        }

        fun prepareServiceParam(): String {
            return Service(
                title = proposal.get()!!,
                date = System.currentTimeMillis(),
                index = emptyList(),
                chainService = ChainService(
                    userWalletAddress = uiState.value.profile.userWalletAddress
                )
            ).toJson()
        }
    }
}