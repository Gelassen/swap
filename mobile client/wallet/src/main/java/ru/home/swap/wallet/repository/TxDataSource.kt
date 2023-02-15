package ru.home.swap.wallet.repository

import android.app.Application
import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.home.swap.core.extensions.registerIdlingResource
import ru.home.swap.core.extensions.unregisterIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.storage.ChainTransactionDao
import ru.home.swap.wallet.storage.toDomain

class TxDataSource(val application: Application, val dao: ChainTransactionDao, val pageSize: Int)
    : PagingSource<Int, ITransaction>()  {

    companion object {
        const val DEFAULT_START_PAGE = 0
    }

    private val logger = Logger.getInstance()

    override fun getRefreshKey(state: PagingState<Int, ITransaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ITransaction> {
        val page = params.key ?: DEFAULT_START_PAGE

        return try {
            logger.d("[TxDataSource] start loadSize ${params.loadSize} and offset ${page * params.loadSize}")
            application.registerIdlingResource()
            val entities = dao.getByPage(params.loadSize, page * params.loadSize)

            LoadResult.Page(
                data = entities.map { it.toDomain() },
                prevKey = if (page == DEFAULT_START_PAGE) null else page.minus(1),
                nextKey = if (entities.isEmpty()) null else page.plus(params.loadSize / pageSize)
            )
        } catch (e: Exception) {
            logger.e("Failed to get tx cached data", e)
            LoadResult.Error(e)
        } finally {
            application.unregisterIdlingResource()
        }
    }
}