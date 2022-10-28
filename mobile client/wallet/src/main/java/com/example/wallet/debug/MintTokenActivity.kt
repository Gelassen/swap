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
import com.example.wallet.debug.repository.StorageRepository
import com.example.wallet.debug.repository.WalletRepository
import com.example.wallet.debug.storage.AppDatabase
import com.example.wallet.debug.storage.ChainTransactionDao
import kotlinx.coroutines.launch
import ru.home.swap.core.logger.Logger
import java.math.BigInteger

class MintTokenActivity: AppCompatActivity() {

    private val logger: Logger = Logger.getInstance()
    // TODO figure out di management for multi-modules project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mint_token_activity)

        val viewModel: WalletViewModel by viewModels()
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
                viewModel.getTokens(account)
            }

        findViewById<Button>(R.id.testNewFunctionality)
            .setOnClickListener {
                val repo = WalletRepository(this)
                lifecycleScope.launch {
                    logger.d("start test")
                    repo.test()
                        .collect { it ->
                            logger.d("collect call")
                            logger.d(it.topics.toString())
                            logger.d("hash ${it.transactionHash}")
                        }
                    logger.d("end test")
                }
/*                val to = getString(R.string.my_account)
                val value = Value(
                    "Consulting2",
                    BigInteger.valueOf(1665158348220),
                    BigInteger.valueOf(1667758348220),
                    false,
                    BigInteger.valueOf(0)
                )
                val uri = "https://gelassen.github.io/blog/"
                val chainTxDao = AppDatabase.getInstance(applicationContext).chainTransactionDao()
                lifecycleScope.launch {
*//*                    StorageRepository(chainTxDao)
                        .createChainTx(to, value, uri)*//*
                    StorageRepository(chainTxDao)
                        .getAllChainTransactions()
                        .collect { it ->
                            logger.d("Collect for chain tx is called ${it.toString()}")
                        }
                }*/
            }
    }
}