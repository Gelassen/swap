package ru.home.swap.ui.offers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.*
import ru.home.swap.R
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.repository.pagination.MatchesPagingSource
import javax.inject.Inject

data class Model(
    val pagingData: PagingData<Any>? = null,
    val profile: PersonProfile? = null,
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
)

@Deprecated("Use OffersV2ViewModel instead. It supports on-chain operations")
class OffersViewModel
@Inject constructor(
    private val matchesPagingSource: MatchesPagingSource,
    private val repository: IPersonRepository,
    private val application: Context
): ViewModel() {

    private val state: MutableStateFlow<Model> = MutableStateFlow(Model())
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
                            val errorMsg = application.getString(R.string.no_demands_in_user_profile)
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

    private fun getPagingData(profile: PersonProfile): Flow<PagingData<Any>> {
        matchesPagingSource.setCredentials(profile.contact, profile.secret)
        val pageSize = application.getString(R.string.page_size).toInt()
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