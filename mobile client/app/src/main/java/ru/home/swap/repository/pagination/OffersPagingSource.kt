package ru.home.swap.repository.pagination

import android.app.Application
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.home.swap.App
import ru.home.swap.core.extensions.registerIdlingResource
import ru.home.swap.core.extensions.unregisterIdlingResource
import ru.home.swap.core.model.Service
import ru.home.swap.core.network.IApi
import ru.home.swap.utils.AppCredentials
import java.io.IOException
import java.util.*
import kotlin.IllegalStateException

class OffersPagingSource(
    private val application: Application,
    private val api: IApi,
    private val pageSize: Int)
    : PagingSource<Int, Service>() {

    companion object {
        const val DEFAULT_START_PAGE = 1
    }

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
            application.registerIdlingResource()
            val page = params.key ?: DEFAULT_START_PAGE

            val response = api.getOffers(
                credentials = AppCredentials.basic(contact, secret),
                page = page,
                size = pageSize
            )
            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.payload?.toList() ?: Collections.emptyList()
                Log.d(App.PAGING, "Paging call")
                return LoadResult.Page(
                    data = data,
                    prevKey = if (page == DEFAULT_START_PAGE) null else  page.minus(1),
                    nextKey = if (data.isEmpty())  null else page.plus(params.loadSize / pageSize)
                )
            } else {
                return LoadResult.Error(IllegalStateException("Server returned response, but it wasn't successful."))
            }
        } catch (ex: IOException) {
            return LoadResult.Error(ex)
        } catch (ex: HttpException) {
            return LoadResult.Error(ex)
        }
        finally {
            application.unregisterIdlingResource()
        }
    }

    fun setCredentials(contact: String, secret: String) {
        this.contact = contact
        this.secret = secret
    }

}