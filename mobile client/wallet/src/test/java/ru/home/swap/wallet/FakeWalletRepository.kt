package ru.home.swap.wallet

import ru.home.swap.wallet.contract.SwapValue
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.repository.IWalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import java.math.BigInteger

class FakeWalletRepository: IWalletRepository {

    override fun balanceOf(owner: String): Flow<BigInteger> {
        return flow {
            emit(BigInteger.valueOf(42L))
        }
    }

    @Throws(TransactionException::class)
    override fun mintToken(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>> {
        return flow {
            throw TransactionException(
                "Transaction receipt was not generated after 600 seconds for transaction",
                ""
            )
        }
    }

    override fun mintToken(
        to: String,
        value: Value,
        uri: String,
        wei: BigInteger
    ): Flow<TransactionReceipt> {
        TODO("Not yet implemented")
    }

    override fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse> {
        TODO("Not yet implemented")
    }

    override fun getTransferEvents(): Flow<SwapValue.TransferEventResponse> {
        TODO("Not yet implemented")
    }

    override fun getOffer(tokenId: String): Value {
        TODO("Not yet implemented")
    }

    override fun registerUserOnSwapMarket(userWalletAddress: String) {
        TODO("Not yet implemented")
    }
}