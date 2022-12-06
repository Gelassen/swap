package ru.home.swap.wallet.repository

import ru.home.swap.wallet.contract.SwapValue
import ru.home.swap.wallet.contract.Value
import kotlinx.coroutines.flow.Flow
import org.bouncycastle.asn1.ocsp.ResponseData
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import java.math.BigInteger

interface IWalletRepository {

    companion object {
        const val TX_STATUS_FAIL = "0x0"
        const val TX_STATUS_OK = "0x1"
    }

    fun balanceOf(owner: String): Flow<Response<BigInteger>>

    @Throws(TransactionException::class)
    fun mintToken(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>>

    fun mintToken(to: String, value: Value, uri: String, wei: BigInteger): Flow<TransactionReceipt>

    fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse>

    fun getTransferEvents(): Flow<SwapValue.TransferEventResponse>

    fun getOffer(tokenId: String): Value

    fun registerUserOnSwapMarket(userWalletAddress: String): Flow<Response<TransactionReceipt>>

    fun approveTokenManager(operator: String, approved: Boolean): Flow<Response<TransactionReceipt>>
}