package ru.home.swap.repository.pagination

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.home.swap.App
import ru.home.swap.core.model.Service
import ru.home.swap.core.network.IApi
import ru.home.swap.utils.AppCredentials
import java.io.IOException
import java.util.*
import kotlin.IllegalStateException
import kotlin.collections.ArrayList

class MatchesPagingSource(private val api: IApi, private val pageSize: Int)
    : PagingSource<Int, Any>() {

    companion object {
        const val DEFAULT_START_PAGE = 1
    }

    private var contact: String? = null
    private var secret: String? = null

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        if (contact == null || secret == null)
            throw IllegalStateException("Credentials are empty. Did you pass credentials for API request?")

        try {
            val page = params.key ?: DEFAULT_START_PAGE

            val response = api.getMatchesForUserDemands(
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
    }

    fun setCredentials(contact: String, secret: String) {
        this.contact = contact
        this.secret = secret
    }

}