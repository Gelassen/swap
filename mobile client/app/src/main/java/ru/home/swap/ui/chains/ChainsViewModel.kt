package ru.home.swap.ui.chains

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.logger.Logger
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.repository.IStorageRepository
import javax.inject.Inject
import javax.inject.Named

data class Model(
    val isLoading: Boolean = false,
    val pagedData: PagingData<ITransaction>? = null
)

class ChainsViewModel
@Inject constructor(
    private val app: Application,
    private val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AndroidViewModel(app) {

    private val logger = Logger.getInstance()

    private val state = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun loadByPage() {
        viewModelScope.launch {
            cacheRepository
                .getChainTransactionsByPage()
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flowOn(backgroundDispatcher)
                .collect { it ->
                    state.update { state ->
                        logger.d("[loadByPage] collect result")
                        state.copy(isLoading = false, pagedData = it)
                    }
                }
        }

    }

}