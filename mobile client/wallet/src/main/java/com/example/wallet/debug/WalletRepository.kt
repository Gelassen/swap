package com.example.wallet.debug

import android.content.Context
import com.example.wallet.R
import com.example.wallet.debug.contract.ISwapValue
import com.example.wallet.debug.contract.SwapValue
import com.example.wallet.debug.contract.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import ru.home.swap.core.logger.Logger
import java.math.BigInteger

class WalletRepository(val context: Context) {

    private val logger: Logger = Logger.getInstance()

    private lateinit var web3: Web3j
    private lateinit var swapValueContract: ISwapValue

    init {
        web3 = Web3j.build(HttpService(context.getString(R.string.test_infura_api)))
        runBlocking {
            launch(Dispatchers.IO) {
                loadContract()
            }
        }
    }

    fun balanceOf(owner: String): Flow<BigInteger> {
        return flow {
            val balance = swapValueContract.balanceOf(owner).send()
            logger.d("balanceOf() call result ${balance}")
            emit(balance)
        }
    }

    fun mintToken(to: String, value: Value, uri: String): Flow<TransactionReceipt> {
        // TODO web3.ethEstimateGas() would require Transaction.createFunctionCallTransaction()
        // TODO figure out why passed wei is not added in total gas used by tx; error message is `has failed with status: 0x0. Gas used: 24397.`
        return mintToken(to, value, uri, BigInteger.valueOf(423220))
    }

    fun mintToken(to: String, value: Value, uri: String, wei: BigInteger): Flow<TransactionReceipt> {
        return flow { // TODO wei is omitted in web3j-cli generate wrapper
            val txReceipt = swapValueContract.safeMint(to, value, uri).send()
            logger.d("safeMint() txReceipt ${txReceipt}")
            emit(txReceipt)
        }
    }

    private fun estimateGas(function: Function) : BigInteger {
//        Transaction.createFunctionCallTransaction()
        return BigInteger.valueOf(0)
    }

    private suspend fun loadContract() = withContext(Dispatchers.IO) {
        val swapValueContractAddress: String = context.getString(R.string.swap_value_contract_address)
/*      // for infura testnet custom tx manager would require sign tx before sending it to the node,
        // otherwise it will throw an error 'The method eth_sendTransaction does not exist/is not available'
        // (it was a test of workaround regarding timeout exception issue)
        val sleepDuration: Int = 15 * 1000
        val attempts: Int = 8
        val txManager: TransactionManager = ClientTransactionManager(
            web3,
            context.getString(R.string.my_account),
            attempts,
            sleepDuration
        )*/
        swapValueContract = SwapValue.load(swapValueContractAddress, web3, getCredentials(), DefaultGasProvider())
    }

    private fun getCredentials() : Credentials {
        return Credentials.create(context.getString(R.string.wallet_password))
    }

}