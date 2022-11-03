package com.example.wallet.debug.repository

import android.content.Context
import com.example.wallet.R
import com.example.wallet.debug.contract.SwapChain
import com.example.wallet.debug.contract.SwapValue
import com.example.wallet.debug.contract.Value
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
import java.math.BigInteger
import java.util.*


class WalletRepository(
    val context: Context,
    httpService: HttpService
) : IWalletRepository {

    private val logger: Logger = Logger.getInstance()

    private var web3: Web3j
    private lateinit var swapValueContract: SwapValue
    private lateinit var swapChainContract: SwapChain

    init {
        web3 = Web3j.build(httpService)
        runBlocking {
            launch(Dispatchers.IO) {
                loadContract()
            }
        }
    }

    override fun balanceOf(owner: String): Flow<BigInteger> {
        return flow {
            val balance = swapValueContract.balanceOf(owner).send()
            logger.d("balanceOf() call result ${balance}")
            emit(balance)
        }
    }

    @Throws(TransactionException::class)
    override fun mintToken(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>> {
        /*
        * web3.ethEstimateGas() would require Transaction.createFunctionCallTransaction()
        * wei is omitted in web3j-cli generate wrapper,
        * */
        return flow {
            val txReceipt = swapValueContract.safeMint(to, value, uri).send()
            logger.d("safeMint() txReceipt ${txReceipt}")
            emit(Response.Data<TransactionReceipt>(txReceipt))
        }
    }

    override fun mintToken(to: String, value: Value, uri: String, wei: BigInteger): Flow<TransactionReceipt> {
        return flow {
            val txReceipt = swapValueContract.safeMint(to, value, uri, wei).send()
            logger.d("safeMint() txReceipt ${txReceipt}")
            emit(txReceipt)
        }
    }

    override fun getTokensNotConsumedAndBelongingToMe(account: String): Flow<SwapValue.TransferEventResponse> {
        return getTransferEvents()
            .filter { it ->
                it.to?.lowercase().equals(account.lowercase())
            }
    }

    override fun getTransferEvents(): Flow<SwapValue.TransferEventResponse> {
        logger.d("start getTransferEvents()")
        val swapValueContractAddress: String = context.getString(R.string.swap_value_contract_address)
        val ethFilter = EthFilter(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST,
            swapValueContractAddress
        ).addSingleTopic(EventEncoder.encode(SwapValue.Const.TRANSFER_EVENT))
        /*
        * There is an issue with using Kotlin version of TRANSFER_EVENT and java wrapper's one
        *
        * See more information on topic structure and how to decipher EVENT type (topic.get(0))
        * https://ethereum.stackexchange.com/questions/64856/web3j-how-to-get-event-args-when-parsing-logs
        * */
        return swapValueContract.transferEventFlowable(ethFilter)
            .subscribeOn(Schedulers.io())
            .asFlow()
    }

    private suspend fun loadContract() = withContext(Dispatchers.IO) {
        val swapValueContractAddress: String = context.getString(R.string.swap_value_contract_address)
        swapValueContract = SwapValue.load(
            swapValueContractAddress,
            web3,
            getCredentials(),
            DefaultGasProvider()
        )
        val swapChainContractAddress: String = context.getString(R.string.swap_chain_contract_address)
        swapChainContract = SwapChain.load(
            swapChainContractAddress,
            web3,
            getCredentials(),
            DefaultGasProvider()
        )
    }

    private fun getCredentials() : Credentials {
        return Credentials.create(context.getString(R.string.wallet_password))
    }

    override fun getOffer(tokenId: String): Value {
        return swapValueContract.getOffer(tokenId).send()
    }

    override fun registerUserOnSwapMarket(userWalletAddress: String) {
        swapValueContract
    }

}