package ru.home.swap.repository.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.home.swap.App
import ru.home.swap.model.Service
import ru.home.swap.network.IApi
import ru.home.swap.repository.Cache
import ru.home.swap.utils.AppCredentials
import java.io.IOException
import java.util.*
import kotlin.IllegalStateException
import kotlin.collections.ArrayList

const val DEFAULT_START_PAGE = 1

class OffersPagingSource(private val api: IApi, private val cache: Cache)
    : PagingSource<Int, Service>() {

    private var contact: String? = null
    private var secret: String? = null

    override fun getRefreshKey(state: PagingState<Int, Service>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Service> {
        if (contact == null || secret == null)
            throw IllegalStateException("Credentials are empty. Did you pass credentials for API request?")

        try {
            val page = params.key ?: DEFAULT_START_PAGE

            val response = api.getOffers(
                credentials = AppCredentials.basic(contact, secret),
                page = page
            )
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.payload?.asList() ?: Collections.emptyList()
                return LoadResult.Page(
                    data = data,
                    prevKey = if (page == DEFAULT_START_PAGE) null else  page.minus(1),
                    nextKey = if (data.isEmpty())  null else page.plus(params.loadSize / App.Config.getPageSize())
                )
            } else {
                return LoadResult.Error(IllegalStateException("Server returned response, but it wasn't successful."))
            }
        } catch (ex: IOException) {
            return LoadResult.Error(ex)
        } catch (ex: HttpException) {
            return LoadResult.Error(ex)
        }
    }

    fun setCredentials(contact: String, secret: String) {
        this.contact = contact
        this.secret = secret
    }

    private fun Collection<Service>.asList(): List<Service> {
        return ArrayList<Service>(this)
    }

}