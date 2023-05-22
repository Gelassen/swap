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
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.model.toDomain
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
                loadContract(R.string.third_acc_private_key)
            }
        }
    }

    override fun balanceOf(owner: String): Flow<Response<BigInteger>> {
        return flow {
            try {
                logger.d("[start] balanceOf() for owner $owner")
                val balance = swapValueContract.balanceOf(owner).send()
                emit(Response.Data(balance))
            } catch (e: Exception) {
                logger.e("Failed to call balanceOf()", e)
                emit(Response.Error.Exception(e))
            } finally {
                logger.d("[end] balanceOf()")
            }
        }
    }

    @Deprecated("Non-Flow implementation is used instead")
    @Throws(TransactionException::class)
    override fun mintTokenAsFlow(to: String, value: Value, uri: String): Flow<Response<TransactionReceipt>> {
        /*
        * web3.ethEstimateGas() would require Transaction.createFunctionCallTransaction()
        * wei is omitted in web3j-cli generate wrapper,
        * */
        return flow {
            try {
                logger.d("[start] mintToken() $to, $value, $uri")
                val txReceipt = swapValueContract.safeMint(to, value, uri).send()
                if (txReceipt.isStatusOK) {
                    emit(Response.Data<TransactionReceipt>(txReceipt))
                } else {
                    emit(Response.Error.Message("Reverted with reason: ${txReceipt.revertReason}"))
                }
            } catch (ex: Exception) {
                logger.e("Failed to mint a token", ex)
                emit(Response.Error.Exception(ex))
            } finally {
                logger.d("[end] mintToken()")
            }

        }
    }

    override suspend fun mintToken(to: String, value: Value, uri: String): Response<TransactionReceiptDomain> {
        lateinit var response: Response<TransactionReceiptDomain>
        try {
            logger.d("[start] mintToken() $to, $value, $uri")
            val txReceipt = swapValueContract.safeMint(to, value, uri).send()
            response = Response.Data<TransactionReceiptDomain>(txReceipt.toDomain())
        } catch (ex: Exception) {
            logger.e("Failed to mint a token", ex)
            response = Response.Error.Exception(ex)
        } finally {
            logger.d("[end] mintToken()")
        }
        return response
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
            lateinit var result: Response<List<*>>
            try {
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response = swapValueContract.getTokensIdsForUser(userWalletAddress).send()
                    result = Response.Data(response)
                } else {
                    result = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you pass a correct ethereum address.")
                }
            } catch (ex: Exception) {
                logger.e("Failed to obtain tokens ids for user", ex)
                result = Response.Error.Exception(ex)
            } finally {
                logger.d("[end] getTokenIdsForUser() for user ${userWalletAddress}")
            }
            emit(result)
        }
    }

    override fun getTokenIdsWithValues(
        userWalletAddress: String,
        withConsumed: Boolean
    ): Flow<Response<List<Token>>> {
        return flow {
            logger.d("[start] getTokenIdsWithValues() for user ${userWalletAddress}")
            lateinit var result: Response<List<Token>>
            try {
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response = swapValueContract.getTokensIdsForUser(userWalletAddress).send()
                    val finalResult = getFullTokenForTokenIds(response, withConsumed)
                    result = Response.Data(finalResult)
                } else {
                    val response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you pass a correct ethereum address.")
                    result = response
                }
            } catch (ex: Exception) {
                logger.e("Failed to obtain tokens ids for user", ex)
                result = Response.Error.Exception(ex)
            } finally {
                logger.d("[end] getTokenIdsWithValues() for user ${userWalletAddress}")
            }
            emit(result)
        }
    }

    @Deprecated("Non-Flow implementation is used instead")
    override fun swapAsFlow(subj: Match): Flow<Response<TransactionReceipt>> {
        return flow {
            try {
                logger.d("[start] swap call")
                val response = swapChainContract.swap(subj).send()
                if (response.isStatusOK) {
                    emit(Response.Data(response))
                } else {
                    emit(Response.Error.Message("Reverted with reason: ${response.revertReason}"))
                }
            } catch (ex: Exception) {
                emit(Response.Error.Exception(ex))
            } finally {
                logger.d("[end] swap call")
            }
        }
    }

    override suspend fun swap(subj: Match): Response<TransactionReceiptDomain> {
        lateinit var result: Response<TransactionReceiptDomain>
        try {
            logger.d("[start] swap call")
            val response = swapChainContract.swap(subj).send()
            if (response.isStatusOK) {
                result = Response.Data(response.toDomain())
            } else {
                result = Response.Error.Message("Reverted with reason: ${response.revertReason}")
            }
        } catch (ex: Exception) {
            result = Response.Error.Exception(ex)
        } finally {
            logger.d("[end] swap call")
        }
        return result
    }

    override suspend fun getMatches(userFirst: String, userSecond: String): Response<List<Match>> {
        logger.d("[start] getMatches()")
        lateinit var result: Response<List<Match>>
        try {
            val response = swapChainContract.getMatches(userFirst, userSecond).send()
            result = Response.Data(response)
        } catch (ex: Exception) {
            result = Response.Error.Exception(ex)
        }
        logger.d("[end] getMatches()");
        return result;
    }

    override fun getMatchesAsFlow(userFirst: String, userSecond: String): Flow<Response<List<Match>>> {
        return flow {
            logger.d("[start] getMatches() ${userFirst} and ${userSecond}")
            lateinit var result: Response<List<Match>>
            try {
                val response = swapChainContract.getMatches(userFirst, userSecond).send()
                result = Response.Data(response)
            } catch (ex: Exception) {
                result = Response.Error.Exception(ex)
            }
            logger.d("[end] getMatches()");
            emit(result)
        }
    }

    override fun getOffer(tokenId: String): Flow<Response<Value>> {
        return flow {
            logger.d("[start] getOffer() for tokenId $tokenId")
            lateinit var result: Response<Value>
            try {
                val response = swapValueContract.getOffer(tokenId).send()
                result = Response.Data(response)
            } catch (ex: Exception) {
                result = Response.Error.Exception(ex)
            } finally {
                logger.d("[end] getOffer()")
            }
            emit(result)
        }
    }

    @Deprecated("Non-Flow implementation is used instead")
    override fun registerUserOnSwapMarketAsFlow(userWalletAddress: String): Flow<Response<TransactionReceipt>> {
        return flow {
            logger.d("[start] registerUserOnSwapMarket")
            val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                    && userWalletAddress.uppercase().contentEquals(
                        Keys.toChecksumAddress(userWalletAddress).uppercase())
            lateinit var response: Response<TransactionReceipt>
            try {
                if (isValidEthAddress) {
                    val txReceipt: TransactionReceipt = swapChainContract.registerUser(userWalletAddress).send()
                    logger.d("Response from registerUser() + ${txReceipt}")
                    response = Response.Data(txReceipt)
                } else {
                    response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you have passed a correct ethereum address.")
                }
            } catch (ex: Exception) {
                response = Response.Error.Exception(ex)
            } finally {
                logger.d("[end] registerUserOnSwapMarket")
            }
            emit(response)
        }
    }

    override suspend fun registerUserOnSwapMarket(userWalletAddress: String): Response<TransactionReceiptDomain> {
        logger.d("[start] registerUserOnSwapMarket")
        val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                && userWalletAddress.uppercase().contentEquals(
            Keys.toChecksumAddress(userWalletAddress).uppercase())
        lateinit var response: Response<TransactionReceiptDomain>
        try {
            if (isValidEthAddress) {
                val txReceipt: TransactionReceipt = swapChainContract.registerUser(userWalletAddress).send()
                logger.d("Response from registerUser() + ${txReceipt}")
                response = Response.Data(txReceipt.toDomain())
            } else {
                response = Response.Error.Message("${userWalletAddress} is not valid ethereum address.Please check you have passed a correct ethereum address.")
            }
        } catch (ex: Exception) {
            logger.e("Get an exception on registerUserOnSwapMarket() call", ex)
            response = Response.Error.Exception(ex)
        } finally {
            logger.d("[end] registerUserOnSwapMarket")
        }
        return response
    }

    @Deprecated("Non-Flow implementation is used instead")
    override fun approveTokenManagerAsFlow(operator: String, approved: Boolean): Flow<Response<TransactionReceipt>> {
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

    override suspend fun approveTokenManager(operator: String, approved: Boolean): Response<TransactionReceiptDomain> {
        lateinit var result: Response<TransactionReceiptDomain>
        try {
            logger.d("[start] approve token manager")
            val response: TransactionReceipt = swapValueContract.setApprovalForAll(operator, approved).send()
            result = Response.Data(response.toDomain())
        } catch (ex: Exception) {
            logger.e("Failed to approve address as an operator over the tokens for this user", ex)
            result = Response.Error.Exception(ex)
        } finally {
            logger.d("[end] approve token manager")
        }
        return result
    }

    @Deprecated("Non-Flow implementation is used instead")
    override fun approveSwapAsFlow(matchSubj: Match): Flow<Response<TransactionReceipt>> {
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

    override suspend fun approveSwap(matchSubj: Match): Response<TransactionReceiptDomain> {
        lateinit var result: Response<TransactionReceiptDomain>
        try {
            logger.d("[start] approve swap")
            val response: TransactionReceipt = swapChainContract.approveSwap(
                matchSubj.userFirst,
                matchSubj.userSecond,
                matchSubj
            )
                .send()
            result = Response.Data(response.toDomain())
        } catch (ex: Exception) {
            logger.e("Failed to approve match", ex)
            val response = Response.Error.Exception(ex)
            result = response
        } finally {
            logger.d("[end] approve swap")
        }
        return result
    }

    @Deprecated("Register demand is not supported since V2")
    override fun registerDemandAsFlow(userWalletAddress: String, demand: String): Flow<Response<TransactionReceipt>> {
        return flow {
            try {
                logger.d("[start] register demand")
                val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                        && userWalletAddress.uppercase().contentEquals(
                    Keys.toChecksumAddress(userWalletAddress).uppercase())
                if (isValidEthAddress) {
                    val response: TransactionReceipt = swapChainContract.registerDemand(userWalletAddress, demand).send()
                    if (response.isStatusOK) {
                        emit(Response.Data(response))
                    } else {
                        emit(Response.Error.Message("Reverted with reason: ${response.revertReason}"))
                    }
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

    @Deprecated("Register demand is not supported since V2")
    override suspend fun registerDemand(userWalletAddress: String, demand: String): Response<TransactionReceiptDomain> {
        lateinit var result: Response<TransactionReceiptDomain>
        try {
            logger.d("[start] register demand")
            val isValidEthAddress = WalletUtils.isValidAddress(userWalletAddress)
                    && userWalletAddress.uppercase().contentEquals(
                Keys.toChecksumAddress(userWalletAddress).uppercase())
            if (isValidEthAddress) {
                val response: TransactionReceipt = swapChainContract.registerDemand(userWalletAddress, demand).send()
                if (response.isStatusOK) {
                    result = Response.Data(response.toDomain())
                } else {
                    result = Response.Error.Message("Reverted with reason: ${response.revertReason}")
                }
            } else {
                val errMessage = "${userWalletAddress} is not valid ethereum address."
                result = Response.Error.Message(errMessage)
            }
        } catch (ex: Exception) {
            logger.e("Failed to approve match", ex)
            result = Response.Error.Exception(ex)
        } finally {
            logger.d("[end] register demand")
        }
        return result
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
        loadContract(R.string.second_acc_private_key)
    }

    /*
    * We do not need to have an option to change the user. Each user will have its own mobile device,
    * own wallet, own contract instance. Just leave it for debug purpose.
    * */
    private suspend fun loadContract(userAccountPrivateKeyReference: Int) = withContext(Dispatchers.IO) {
        /*
        *  chainId required to mint tokens based on new ethereum standard (see https://blog.ethereum.org/2021/03/03/geth-v1-10-0)
        *  it is only available over custom RawTransactionManager which is used for both balanceOf() and mint() methods
        * */
        val swapValueContractAddress: String = context.getString(R.string.swap_value_contract_address)
        val chainId: Long = context.getString(R.string.chain_id).toLong()
        swapValueContract = SwapValue.load(
            swapValueContractAddress,
            web3,
            RawTransactionManager(web3, getCredentials(userAccountPrivateKeyReference), chainId),
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
        return getCredentials(R.string.second_acc_private_key)
    }

    private fun getCredentials(userAccountPrivateKeyReference: Int): Credentials {
        return Credentials.create(context.getString(userAccountPrivateKeyReference))
    }

}