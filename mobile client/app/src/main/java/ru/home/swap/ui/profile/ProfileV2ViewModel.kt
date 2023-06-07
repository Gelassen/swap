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
import ru.home.swap.core.model.*
import ru.home.swap.core.network.Response
import ru.home.swap.network.RegisterUserWorker
import ru.home.swap.providers.PersonProvider
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.model.BurnTransaction
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.model.RegisterUserTransaction
import ru.home.swap.wallet.network.BaseChainWorker
import ru.home.swap.wallet.network.MintTokenWorker
import ru.home.swap.wallet.network.BurnTokenWorker
import ru.home.swap.wallet.network.getWorkRequest
import ru.home.swap.wallet.providers.WalletProvider
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.model.DataItemFromView
import ru.home.swap.wallet.storage.model.ServerTransaction
import ru.home.swap.wallet.storage.model.TxStatus
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Named

data class ModelV2(
    var profile: PersonProfile = PersonProfile(),
    var pendingTx: ConcurrentLinkedQueue<Pair<ITransaction, ServerTransaction>> = ConcurrentLinkedQueue(),
    var pendingTxFromView: ConcurrentLinkedQueue<DataItemFromView> = ConcurrentLinkedQueue(),
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

    /**
     * TODO: [ready for tests] remove offer
     * 1. cache chain and server tx
     * 2. execute burn() on-chain
     * 3. get tx from queue and do backend remove()
     * 4. in a backend response the client will get person profile which should be updated in cache
     *
     * For test:
     * 1. Test burn() on-chain call in isolated environment of TestWalletActivity
     * 2. Run a whole process to confirm it works
     * */

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
    fun backgroundProcessMinedTx() {
        val useCase = BackgroundFlowUseCase()
        viewModelScope.launch {
            useCase.waitUntilGetAnItem()
                .flatMapConcat { item ->
                    when(item.first.type) {
                        /* token are created only for offer, demands registered just as a db record */
                        MintTransaction::class.java.simpleName -> { useCase.processMintTx(item) }
                        RegisterUserTransaction::class.java.simpleName -> { useCase.processRegisterUserTx(item) }
                        BurnTransaction::class.java.simpleName -> { useCase.processBurnTransaction(item) }
                        else -> { throw UnsupportedOperationException("Did you forget to add support of ${item.first.type} class tx?") }
                    }
                }
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat {
                    if (it is Response.Data) { useCase.updateCacheForCompletedTx(it)}
                    flow { emit(it) }
                }
                .collect { it ->
                    Log.d(App.TAG, "[add offer] start collect data in viewmodel")
                    processServerResponse(it) { defaultSpecialHandler(it) }
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
            val workManager = WorkManager.getInstance(app)

            val requestBuilder = RequestBuilder()
            val work = MintTokenWorker.Builder.build(
                requestBuilder.prepareToAddress(),
                requestBuilder.prepareValueParam(),
                assignedUri,
                requestBuilder.prepareServiceParam()
            )
            val workRequest = workManager.getWorkRequest<MintTokenWorker>(work)
            workManager.enqueue(workRequest)
            workManager
                .getWorkInfoByIdLiveData(workRequest.id)
                .asFlow()
                .onCompletion {  state.update { state -> state.copy(isLoading = false) } }
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> { logger.d("[mint token] succeeded status with result: ${it.toString()}")}
                        WorkInfo.State.FAILED -> {
                            logger.d("[mint token] failed status with result: ${it.toString()}")
                            var error = ""
                            if (it.outputData.keyValueMap.containsKey(BaseChainWorker.Consts.KEY_ERROR_MSG)) {
                                error = it.outputData.keyValueMap.get(BaseChainWorker.Consts.KEY_ERROR_MSG) as String
                            } else {
                                error = "Something went wrong during minting the token"
                            }
                            state.update { state -> state.copy(errors = state.errors.plus(error)) }
                        }
                        else -> { logger.d("[mint token] unexpected state with result: ${it.toString()}") }
                    }
                }
        }
        logger.d("[end] mintToken()")
    }

    fun removeOffer(service: Service) {
        viewModelScope.launch {
            val workManager = WorkManager.getInstance(app)
            val work = BurnTokenWorker.Builder.build(
                uiState.value.profile.userWalletAddress,
                service.chainService.tokenId,
                service.toJson()
            )
            val workRequest = workManager.getWorkRequest<BurnTokenWorker>(work)
            workManager.enqueue(workRequest)
            workManager
                .getWorkInfoByIdLiveData(workRequest.id)
                .asFlow()
                .onCompletion {  state.update { state -> state.copy(isLoading = false) } }
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> { logger.d("[burn token] succeeded status with result: ${it}")}
                        WorkInfo.State.FAILED -> {
                            logger.d("[burn token] failed status with result: ${it}")
                            var error = ""
                            if (it.outputData.keyValueMap.containsKey(BaseChainWorker.Consts.KEY_ERROR_MSG)) {
                                error = it.outputData.keyValueMap.get(BaseChainWorker.Consts.KEY_ERROR_MSG) as String
                            } else {
                                error = "Something went wrong during burn the token"
                            }
                            state.update { state -> state.copy(errors = state.errors.plus(error)) }
                        }
                        else -> { logger.d("[burn token] unexpected state with result: ${it}") }
                    }
                }
/*            personRepository.removeOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = item.uid)
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
                .collect { it -> processServerResponse(it) { removeOfferSpecialHandler(it) } }*/
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
                id = item.uid)
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
            val errorStr = if (!validator.inputIsEthereumAddress(profile.userWalletAddress)) {
                R.string.bad_eth_address_error
            } else {
                R.string.empty_credentials_error
            }
            val errorMsg = app.getString(errorStr)
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
        logger.d("[start] createAnAccount")
        viewModelScope.launch {
            state.update { state -> state.copy(isLoading = true) }
            val workManager = WorkManager.getInstance(app)
            val work = RegisterUserWorker.Builder.build(person)
            val workRequest = workManager.getWorkRequest<RegisterUserWorker>(work)
            workManager.enqueue(workRequest)
            workManager
                .getWorkInfoByIdLiveData(workRequest.id)
                .asFlow()
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            logger.d("[create an account] succeeded status with result: ${it}")
                            state.update { state ->
                                state.copy(
                                    isLoading = false,
                                    profile = PersonProfile().fromJson(it.outputData.getString(RegisterUserWorker.KEY_PERSON_PROFILE)!!),
                                    status = StateFlagV2.PROFILE
                                )
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            logger.d("[create an account] failed status with result: ${it}")
                            var error = ""
                            if (it.outputData.keyValueMap.containsKey(BaseChainWorker.Consts.KEY_ERROR_MSG)) {
                                error = it.outputData.keyValueMap.get(BaseChainWorker.Consts.KEY_ERROR_MSG) as String
                            } else {
                                error = "Something went during creating an account"
                            }
                            state.update { state -> state.copy(isLoading = false, errors = state.errors.plus(error)) }

                            personRepository.cleanCachedAccount().collect { /* no op */ }
                        }
                        else -> { logger.d("[create an account] unexpected state with result: ${it}") }
                    }
                    state.update { state -> state.copy(isLoading = false) }
                    logger.d("[end] createAnAccount")
                }
        }
    }

    @OptIn(FlowPreview::class)
    fun checkAnExistingAccount() {
        val useCase = CheckExistingAccountFlowUseCase()
        viewModelScope.launch {
            Log.d(App.TAG, "[a] Get cached account call")
            personRepository.getCachedAccount()
                .flatMapConcat { it -> useCase.checkAccount(it) }
                .flatMapConcat { it -> useCase.cacheAccount(it) }
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .catch { e ->
                    Log.e(App.TAG, "Get an error when obtaining account", e)
                    state.update { state ->
                        val errors = state.errors + getErrorMessage(Response.Error.Exception(e))
                        state.copy(errors = errors, isLoading = false)
                    }
                }
                .collect { it ->
                    Log.d(App.TAG, "[c] Get the result")
                    updateState(it)
                    // we have to cancel this call to prevent further callbacks from cache updates
                    // on 'SignIn screen' the correct way is to wait for the response from the chain
                    this.cancel("Just one-shoot call for cached profile preferences")
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
                .map { it -> it.filter { it.first.status == TxStatus.TX_MINED && it.second.status == RequestStatus.WAITING } }
                .flowOn(backgroundDispatcher)
                .catch { ex -> logger.e("Failed to get data from the cache", ex) }
                .collect {
                    logger.d("[loadAllFromCache] 2. Collect result ${it.toString()}")
                    state.update { state ->
                        val newValues = it.subtract(uiState.value.pendingTx)
                        val data = ConcurrentLinkedQueue(state.pendingTx)
                        data.addAll(newValues)
                        state.pendingTx.addAll(newValues) // not sure if this insert changes the state
                        state.copy(pendingTx = data)
                    }
                }
        }
    }

    fun removeShownError() {
        state.update { it ->
            it.copy(
                errors = it.errors.filter { str -> !str.equals(it.errors.first()) }
            )
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

    private fun defaultSpecialHandler(response: Response.Data<PersonProfile>) {
        Log.d(App.TAG, "[offer operation] positive case")
        state.value.profile.offers = response.data.offers
        state.value.profile.demands = response.data.demands
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

        /*
        * Should be called to clean dialog screen from previously entered data
        * */
        fun clearProposal() {
            proposal.set("")
        }

        fun prepareOwner(): String {
            return uiState.value.profile.userWalletAddress
        }

        fun prepareTokenId(subj: SwapMatch): Int {
            return subj.userFirstService.tokenId
        }

        fun prepareService(subj: SwapMatch): String {
            return Service(
                uid = subj.userFirstServiceId.toLong(), // we need just an uid, the rest are stubs
                title = subj.userFirstServiceTitle,
                date = 0,
                index = emptyList(),
                chainService = ChainService()
            ).toJson()
        }
    }

    inner class BackgroundFlowUseCase {

        var currentTxPair: Pair<ITransaction, ServerTransaction>? = null

        fun waitUntilGetAnItem(): Flow< Pair<ITransaction, ServerTransaction>> {
            return flow {
                while(state.value.isAllowedToProcess) {
                    getApplication<Application>().registerIdlingResource()
                    val delay = 3120L
                    // wait for user's wallet address will be obtained from the cache during initialisation
                    if (state.value.profile.userWalletAddress.isEmpty()
                        || state.value.pendingTx.isEmpty()) {
                        logger.d("[queue polling] delay() call")
                        getApplication<Application>().unregisterIdlingResource()
                        delay(delay)
                    }  else {
                        val item = state.value.pendingTx.poll()
                        if (item != null) {
                            logger.d("[queue polling] emit item from queue ${item.toString()}")
                            emit(item)
                        }
                        logger.d("[queue polling] delay() call")
                        getApplication<Application>().unregisterIdlingResource()
                        delay(delay)
                    }
                }
            }
        }

        fun processMintTx(item: Pair<ITransaction, ServerTransaction>): Flow<Response<PersonProfile>> {
            logger.d("[queue polling] process MintTransaction item")
            currentTxPair = item
            (item.second.payload as Service).chainService.tokenId = (item.first as MintTransaction).tokenId
            val newService = item.second.payload
            logger.d("[addOffer::server] ${item.first} \n and \n ${item.second}")
            return personRepository.addOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                newService = newService as Service
            )
        }

        suspend fun updateCacheForCompletedTx(it: Response.Data<PersonProfile>) {
            assert(currentTxPair != null)
            personRepository.cacheAccount(it.data)
            currentTxPair!!.second.status = RequestStatus.PROCESSED
            cacheRepository.createChainTxAndServerTx(
                currentTxPair!!.first,
                currentTxPair!!.second
            )
        }

        fun processRegisterUserTx(item: Pair<ITransaction, ServerTransaction>): Flow<Response<PersonProfile>> {
            currentTxPair = item
            val personProfile = (item.second.payload as PersonProfile)
            return personRepository.createAccountAsFlow(personProfile)
        }

        fun processBurnTransaction(item: Pair<ITransaction, ServerTransaction>): Flow<Response<PersonProfile>> {
            currentTxPair = item
            val serviceId = (item.second.payload as Service).uid
            return personRepository.removeOffer(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
                id = serviceId
            )
        }
    }

    inner class CheckExistingAccountFlowUseCase {
        fun checkAccount(personProfile: PersonProfile) : Flow<Response<PersonProfile>> {
            lateinit var flow: Flow<Response<PersonProfile>>
            if (personProfile.contact.isEmpty() && personProfile.secret.isEmpty()) {
                Log.d(App.TAG, "[b1] Get cached account as an empty")
                flow = flow {
                    emit(Response.Data(personProfile))
                }
            } else {
                Log.d(App.TAG, "[b2] Get cached account call and request server for an actual one")
                flow = personRepository.getAccount(personProfile)
            }
            return flow
        }

        fun cacheAccount(response: Response<PersonProfile>) = flow<Response<PersonProfile>> {
            logger.d("[c] cache account")
            if (response is Response.Data) {
                personRepository.cacheAccount(response.data)
            }
            emit(response)
        }
    }
}