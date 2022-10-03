package com.example.wallet.debug

import android.content.Context
import com.example.wallet.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

class WalletRepository(val context: Context) {

    val testAPI = "https://rinkeby.infura.io/v3/ce67e157fc964d3bbf7ff1db09aa316a"
    private val web3: Web3j = Web3j.build(HttpService(testAPI))

    fun mintToken() : Flow<Int> {
        return flow {
            emit(42)
        }
    }

    private fun getCredentials() : Credentials {
        return Credentials.create(context.getString(R.string.wallet_password))
    }
}