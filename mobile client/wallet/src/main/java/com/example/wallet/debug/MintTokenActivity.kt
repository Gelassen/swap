package com.example.wallet.debug

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wallet.R
import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.di.WalletModule
import com.example.wallet.debug.repository.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.home.swap.core.logger.Logger
import java.math.BigInteger

class MintTokenActivity: AppCompatActivity() {

    private val logger: Logger = Logger.getInstance()
    // TODO figure out di management for multi-modules project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mint_token_activity)

/*        val viewModel: WalletViewModel by viewModels()
        viewModel.setRepository(applicationContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    when(it.status) {
                        Status.NONE -> {
                            logger.d("Origin state")
                        }
                        Status.BALANCE -> {
                            logger.d("Get the contract balance")
                        }
                        Status.MINT_TOKEN -> {
                            logger.d("Mint a new token")
                        }
                    }
                }
            }
        }

        findViewById<Button>(R.id.checkBalance)
            .setOnClickListener {
                viewModel.balanceOf(getString(R.string.my_account))
            }

        findViewById<Button>(R.id.mintToken)
            .setOnClickListener {
                val to = getString(R.string.my_account)
                val value = Value(
                    "Consulting",
                    BigInteger.valueOf(1665158348220),
                    BigInteger.valueOf(1669758348220),
                    false,
                    BigInteger.valueOf(0)
                )
                val uri = "https://gelassen.github.io/blog/"
                viewModel.mintToken(to, value, uri)
            }

        findViewById<Button>(R.id.getMyTokens)
            .setOnClickListener {
                val account = applicationContext.getString(R.string.my_account)
                viewModel.getTokensThatBelongsToMeNotConsumedNotExpired(account)
            }

        findViewById<Button>(R.id.testNewFunctionality)
            .setOnClickListener {
                lifecycleScope.launch {
                    logger.d("start test")
                    testFunc()
                        .collect { it ->
                            logger.d("Result of offer for token id: ${it}")
                    }

                }
            }*/
    }

//    fun testFunc(): Flow<Value> {
//        return flow {
//            val repo = WalletRepository(
//                applicationContext,
//                WalletModule(applicationContext)
//                    .providesWeb3jHttpService(WalletModule(applicationContext).providesInterceptor())
//            )
//            val result = repo.getOffer("12")
//            emit(result)
//        }
//            .flowOn(Dispatchers.IO)
//    }
}