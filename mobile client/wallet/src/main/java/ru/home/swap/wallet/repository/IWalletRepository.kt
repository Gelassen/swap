package ru.home.swap.wallet.repository

import ru.home.swap.wallet.contract.SwapValue
import ru.home.swap.wallet.contract.Value
import kotlinx.coroutines.flow.Flow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.model.Token
import java.math.BigInteger

interface IWalletRepository {

    companion object {
        const val TX_STATUS_FAIL = "0x0"
        const val TX_STATUS_OK = "0x1"
    }

    fun balanceOf(owner: String): Flow<Response<BigInteger>>

    @Throws(TransactionException::class)
    fun mintTokenAsFlow(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>>

    suspend fun mintToken(to: String, value: Value, uri: String): Response<TransactionReceipt>

    fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse>

    fun getTransferEvents(): Flow<SwapValue.TransferEventResponse>

    fun getOffer(tokenId: String): Flow<Response<Value>>

    fun registerUserOnSwapMarketAsFlow(userWalletAddress: String): Flow<Response<TransactionReceipt>>

    suspend fun registerUserOnSwapMarket(userWalletAddress: String): Response<TransactionReceipt>

    fun approveTokenManager(operator: String, approved: Boolean): Flow<Response<TransactionReceipt>>

    fun approveSwap(matchSubj: Match): Flow<Response<TransactionReceipt>>

    fun registerDemand(userWalletAddress: String, demand: String): Flow<Response<TransactionReceipt>>

    fun getTokenIdsForUser(userWalletAddress: String): Flow<Response<List<*>>>

    fun getTokenIdsWithValues(userWalletAddress: String, withConsumed: Boolean): Flow<Response<List<Token>>>

    fun swap(subj: Match): Flow<Response<TransactionReceipt>>

}