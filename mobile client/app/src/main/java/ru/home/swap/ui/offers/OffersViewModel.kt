package ru.home.swap.ui.offers

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.*
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import ru.home.swap.repository.pagination.OffersPagingSource
import javax.inject.Inject

data class Model(
    val profile: PersonProfile? = null,
    val isLoading: Boolean = false,
    val errors: List<String> = emptyList(),
)

class OffersViewModel
@Inject constructor(
    private val repository: PersonRepository,
    private val offersPagingSource: OffersPagingSource,
    private val cache: Cache,
    private val application: Context
): ViewModel() {

    private val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun getOffers(): Flow<PagingData<Service>> {
        return if (uiState.value.profile == null) {
            cache.getProfile()
                .flatMapConcat { it ->
                    state.update { state ->
                        state.copy( profile = it)
                    }
                    getPagingData(state.value.profile!!)
                }
        } else {
            getPagingData(state.value.profile!!)
        }
    }

    private fun getPagingData(profile: PersonProfile): Flow<PagingData<Service>> {
        offersPagingSource.setCredentials(profile.contact, profile.secret)
        val pageSize = application.getString(R.string.page_size).toInt()
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                offersPagingSource
            }
        )
            .flow
    }
}