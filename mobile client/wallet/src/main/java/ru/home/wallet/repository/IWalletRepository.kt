package ru.home.wallet.repository

import com.example.wallet.contract.SwapValue
import com.example.wallet.contract.Value
import kotlinx.coroutines.flow.Flow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import java.math.BigInteger

interface IWalletRepository {

    fun balanceOf(owner: String): Flow<BigInteger>

    @Throws(TransactionException::class)
    fun mintToken(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>>

    fun mintToken(to: String, value: Value, uri: String, wei: BigInteger): Flow<TransactionReceipt>

    fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse>

    fun getTransferEvents(): Flow<SwapValue.TransferEventResponse>

    fun getOffer(tokenId: String): Value

    fun registerUserOnSwapMarket(userWalletAddress: String)
}