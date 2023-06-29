package ru.home.swap.ui.chains

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import ru.home.swap.R
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.repository.pagination.MatchesPagingSource
import javax.inject.Inject
import javax.inject.Named

data class Model(
    val pagingData: PagingData<SwapMatch>? = null,
    var profile: PersonProfile? = null,
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
)

class ChainsViewModel
@Inject constructor(
    private val app: Application,
    private val matchesPagingSource: MatchesPagingSource,
    private val repository: IPersonRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): AndroidViewModel(app) {

    private val logger = Logger.getInstance()

    private val state = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    suspend fun fetchOffers() {
        if (uiState.value.profile == null) {
            repository.getCachedAccount()
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .flatMapConcat { it ->
                    state.update { state ->
                        if (it.demands.isEmpty()) {
                            val errorMsg = app.getString(R.string.no_demands_in_user_profile)
                            state.copy(profile = it, errors = state.errors.plus(errorMsg))
                        } else {
                            state.copy(profile = it)
                        }
                    }
                    getPagingData(state.value.profile!!)
                }
                .collect { it ->
                    state.update { state ->
                        state.copy(
                            pagingData = it,
                            isLoading = false
                        )
                    }
                }
        } else {
            getPagingData(state.value.profile!!)
                .onStart { state.update { state -> state.copy(isLoading = true) } }
                .collect { it ->
                    state.update { state ->
                        state.copy(
                            pagingData = it,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun getPagingData(profile: PersonProfile): Flow<PagingData<SwapMatch>> {
        matchesPagingSource.setCredentials(profile.contact, profile.secret)
        val pageSize = app.getString(R.string.page_size).toInt()
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                matchesPagingSource
            }
        )
            .flow
            .cachedIn(viewModelScope)
    }

    fun removeShownError() {
        /* by convention we always show errors in the natural order (FIFO) */
        state.update { state ->
            state.copy(errors = state.errors.minus(state.errors[0]))
        }
    }

    fun addError(error: String) {
        state.update { state ->
            state.copy(errors = state.errors.plus(error))
        }
    }

}