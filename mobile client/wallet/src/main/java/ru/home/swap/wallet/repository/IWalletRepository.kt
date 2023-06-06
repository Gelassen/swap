package ru.home.swap.wallet.repository

import ru.home.swap.wallet.contract.SwapValue
import ru.home.swap.wallet.contract.Value
import kotlinx.coroutines.flow.Flow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.model.Token
import ru.home.swap.wallet.model.TransactionReceiptDomain
import java.math.BigInteger

interface IWalletRepository {

    companion object {
        const val TX_STATUS_FAIL = "0x0"
        const val TX_STATUS_OK = "0x1"
    }

    fun balanceOf(owner: String): Flow<Response<BigInteger>>

    @Deprecated("Non-Flow implementation is used instead")
    @Throws(TransactionException::class)
    fun mintTokenAsFlow(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>>

    suspend fun mintToken(to: String, value: Value, uri: String): Response<TransactionReceiptDomain>

    fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse>

    fun getTransferEvents(): Flow<SwapValue.TransferEventResponse>

    fun getOffer(tokenId: String): Flow<Response<Value>>

    @Deprecated("Non-Flow implementation is used instead")
    fun registerUserOnSwapMarketAsFlow(userWalletAddress: String): Flow<Response<TransactionReceipt>>

    suspend fun registerUserOnSwapMarket(userWalletAddress: String): Response<TransactionReceiptDomain>

    @Deprecated("Non-Flow implementation is used instead")
    fun approveTokenManagerAsFlow(operator: String, approved: Boolean): Flow<Response<TransactionReceipt>>

    suspend fun approveTokenManager(operator: String, approved: Boolean): Response<TransactionReceiptDomain>

    @Deprecated("Non-Flow implementation is used instead")
    fun approveSwapAsFlow(matchSubj: Match): Flow<Response<TransactionReceipt>>

    suspend fun approveSwap(matchSubj: Match): Response<TransactionReceiptDomain>

    @Deprecated(message = "Register demand is not supported since V2")
    fun registerDemandAsFlow(userWalletAddress: String, demand: String): Flow<Response<TransactionReceipt>>

    @Deprecated(message = "Register demand is not supported since V2")
    suspend fun registerDemand(userWalletAddress: String, demand: String): Response<TransactionReceiptDomain>

    fun getTokenIdsForUser(userWalletAddress: String): Flow<Response<List<*>>>

    fun getTokenIdsWithValues(userWalletAddress: String, withConsumed: Boolean): Flow<Response<List<Token>>>

    @Deprecated("Non-Flow implementation is used instead")
    fun swapAsFlow(subj: Match): Flow<Response<TransactionReceipt>>

    suspend fun swap(subj: Match): Response<TransactionReceiptDomain>

    suspend fun getMatches(userFirst: String, userSecond: String): Response<List<Match>>

    fun getMatchesAsFlow(userFirst: String, userSecond: String): Flow<Response<List<Match>>>

    suspend fun burn(owner: String, tokenId: Long): Response<TransactionReceiptDomain>
}