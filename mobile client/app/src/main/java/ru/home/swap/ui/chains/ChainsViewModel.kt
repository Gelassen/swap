package ru.home.swap.ui.chains

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.repository.PersonRepository
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.repository.IStorageRepository
import javax.inject.Inject
import javax.inject.Named

data class Model(
    val isLoading: Boolean = false,
    val pagedData: PagingData<ITransaction>? = null,
    var profile: PersonProfile = PersonProfile()
)

class ChainsViewModel
@Inject constructor(
    private val app: Application,
    private val cacheRepository: IStorageRepository,
    private val personRepository: IPersonRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AndroidViewModel(app) {

    private val logger = Logger.getInstance()

    private val state = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun getPersonProfile() {
        viewModelScope.launch {
            personRepository
                .getCachedAccount()
                .flowOn(backgroundDispatcher)
                .collect { it ->
                    state.update { state ->
                        state.copy(profile = it)
                    }
                }
        }
    }

    fun loadCachedByPage() {
        viewModelScope.launch {
            cacheRepository
                .getChainTransactionsByPage()
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flowOn(backgroundDispatcher)
                .cachedIn(viewModelScope)
                .collect { it ->
                    state.update { state ->
                        logger.d("[loadByPage] collect result")
                        state.copy(isLoading = false, pagedData = it)
                    }
                }
        }
    }

    fun fetchAggregatedMatches() {
        logger.d("[start] fetchAggregatedMatches")
        viewModelScope.launch {
            // wait for the account instance from cache
            while (uiState.value.profile.contact.isEmpty()) {
                delay(1000L)
                continue
            }

            val matches = personRepository.getAggregatedMatches(
                contact = uiState.value.profile.contact,
                secret = uiState.value.profile.secret,
            )
            logger.d("Get aggregated matches ${matches}")
            // TODO put matches in Room's cache, wait for sample response to
            //  define final table structure

            logger.d("[end] fetchAggregatedMatches")
        }
    }

}