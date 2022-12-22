package ru.home.swap.wallet.repository

import android.content.Context
import com.example.wallet.R
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.web3j.abi.EventEncoder
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.*
import ru.home.swap.wallet.model.Token
import java.math.BigInteger


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
                loadContract(R.string.test_account_private_key)
            }
        }
    }

    override fun balanceOf(owner: String): Flow<Response<BigInteger>> {
        return flow {
            try {
                val balance = swapValueContract.balanceOf(owner).send()
                logger.d("balanceOf() call result ${balance}")
                emit(Response.Data(balance))
            } catch (e: Exception) {
                logger.e("Failed to call balanceOf()", e)
                emit(Response.Error.Exception(e))
            }
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

    override fun getTokenIdsForUser(userWalletAddress: String): Flow<Response<List<*>>> {
        return flow {
            logger.d("[start] getTokenIdsForUser() for user ${userWalletAddress}")
            try {
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response = swapValueContract.getTokensIdsForUser(userWalletAddress).send()
                    emit(Response.Data(response))
                } else {
                    val response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you pass a correct ethereum address.")
                    emit(response)
                }
            } catch (ex: Exception) {
                logger.e("Failed to obtain tokens ids for user", ex)
                emit(Response.Error.Exception(ex))
            } finally {
                logger.d("[end] getTokenIdsForUser() for user ${userWalletAddress}")
            }
        }
    }

    override fun getTokenIdsWithValues(
        userWalletAddress: String,
        withConsumed: Boolean
    ): Flow<Response<List<Token>>> {
        return flow {
            logger.d("[start] getTokenIdsWithValues() for user ${userWalletAddress}")
            try {
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response = swapValueContract.getTokensIdsForUser(userWalletAddress).send()
                    val finalResult = getFullTokenForTokenIds(response, withConsumed)
                    emit(Response.Data(finalResult))
                } else {
                    val response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you pass a correct ethereum address.")
                    emit(response)
                }
            } catch (ex: Exception) {
                logger.e("Failed to obtain tokens ids for user", ex)
                emit(Response.Error.Exception(ex))
            } finally {
                logger.d("[end] getTokenIdsWithValues() for user ${userWalletAddress}")
            }
        }
    }

    override fun swap(subj: Match): Flow<Response<TransactionReceipt>> {
        return flow {
            logger.d("[start] swap call")
            try {
                val response = swapChainContract.swap(subj).send()
                emit(Response.Data(response))
            } catch (ex: Exception) {
                emit(Response.Error.Exception(ex))
            } finally {
                logger.d("[end] swap call")
            }
        }
    }

    private fun getFullTokenForTokenIds(tokenIds: List<*>, withConsumed: Boolean): MutableList<Token> {
        val finalResult = mutableListOf<Token>()
        for (item in tokenIds) {
            val tokenId = (item as BigInteger).toString()
            val value = swapValueContract.getOffer(tokenId).send()
            if (!withConsumed && !value.isConsumed) {
                finalResult.add(Token(tokenId = tokenId.toLong(), value = value))
            } else {
                finalResult.add(Token(tokenId = tokenId.toLong(), value = value))
            }
        }
        return finalResult
    }

    @Deprecated(message = "User loadContract(key) method with explicitly passed user account key")
    private suspend fun loadContract() = withContext(Dispatchers.IO) {
        loadContract(R.string.test_account_private_key)
    }

    /*
    * We do not need to have an option to change the user. Each user will have its own mobile device,
    * own wallet, own contract instance. Just leave it for debug purpose.
    * */
    private suspend fun loadContract(userAccountPrivateKeyReference: Int) = withContext(Dispatchers.IO) {
        // TODO chainId required to mint tokens based on new ethereum standard (see https://blog.ethereum.org/2021/03/03/geth-v1-10-0)
        // it is only available over custom RawTransactionManager which is used for both balanceOf() and mint() methods
        val swapValueContractAddress: String = context.getString(R.string.swap_value_contract_address)
//        val privateKey: String = context.getString(R.string.private_key)
        val chainId: Long = context.getString(R.string.chain_id).toLong()
        swapValueContract = SwapValue.load(
            swapValueContractAddress,
            web3,
            RawTransactionManager(web3, getCredentials(userAccountPrivateKeyReference)/*Credentials.create(privateKey)*/, chainId),
            DefaultGasProvider()
        )
        val swapChainContractAddress: String = context.getString(R.string.swap_chain_contract_address)
        swapChainContract = SwapChain.load(
            swapChainContractAddress,
            web3,
            RawTransactionManager(web3, getCredentials(userAccountPrivateKeyReference), chainId),
            DefaultGasProvider()
        )
    }

    @Deprecated(message = "Use getCredentials(key) with explicitly passed user account key")
    private fun getCredentials() : Credentials {
//        return Credentials.create(context.getString(R.string.test_account_private_key/*R.string.wallet_password*/))
        return getCredentials(R.string.test_account_private_key)
    }

    private fun getCredentials(userAccountPrivateKeyReference: Int): Credentials {
        return Credentials.create(context.getString(userAccountPrivateKeyReference/*R.string.wallet_password*/))
    }

    override fun getOffer(tokenId: String): Value {
        return swapValueContract.getOffer(tokenId).send()
    }

    override fun registerUserOnSwapMarket(userWalletAddress: String): Flow<Response<TransactionReceipt>> {
        return flow {
            logger.d("[start] registerUserOnSwapMarket")
            val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                    && userWalletAddress.uppercase().contentEquals(
                        Keys.toChecksumAddress(userWalletAddress).uppercase())
            if (isValidEthAddress) {
                val response: TransactionReceipt = swapChainContract.registerUser(userWalletAddress).send()
                logger.d("Response from registerUser() + ${response}")
                emit(Response.Data(response))
            } else {
                val response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you pass a correct ethereum address.")
                emit(response)
            }
            logger.d("[end] registerUserOnSwapMarket")
        }

    }

    override fun approveTokenManager(operator: String, approved: Boolean): Flow<Response<TransactionReceipt>> {
        return flow {
            try {
                logger.d("[start] approve token manager")
                val response: TransactionReceipt = swapValueContract.setApprovalForAll(operator, approved).send()
                emit(Response.Data(response))
            } catch (ex: Exception) {
                logger.e("Failed to approve address as an operator over the tokens for this user", ex)
                val response = Response.Error.Exception(ex)
                emit(response)
            } finally {
                logger.d("[end] approve token manager")
            }
        }
    }

    override fun approveSwap(matchSubj: Match): Flow<Response<TransactionReceipt>> {
        return flow {
            try {
                logger.d("[start] approve swap")
                val response: TransactionReceipt = swapChainContract.approveSwap(
                    matchSubj.userFirst,
                    matchSubj.userSecond,
                    matchSubj
                )
                    .send()
                emit(Response.Data(response))
            } catch (ex: Exception) {
                logger.e("Failed to approve match", ex)
                val response = Response.Error.Exception(ex)
                emit(response)
            } finally {
                logger.d("[end] approve swap")
            }
        }
    }

    override fun registerDemand(userWalletAddress: String, demand: String): Flow<Response<TransactionReceipt>> {
        return flow {
            try {
                logger.d("[start] register demand")
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response: TransactionReceipt = swapChainContract.registerDemand(userWalletAddress, demand).send()
                    emit(Response.Data(response))
                } else {
                    val errMessage = "${userWalletAddress} is not valid ethereum address."
                    emit(Response.Error.Message(errMessage))
                }
            } catch (ex: Exception) {
                logger.e("Failed to approve match", ex)
                val response = Response.Error.Exception(ex)
                emit(response)
            } finally {
                logger.d("[end] register demand")
            }
        }
    }

}