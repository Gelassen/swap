package ru.home.swap.wallet

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wallet.R
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.di.WalletDi
import ru.home.swap.wallet.di.WalletModule
import ru.home.swap.wallet.repository.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.logger.Logger
import java.math.BigInteger
import javax.inject.Inject

class MintTokenActivity: AppCompatActivity() {

    private val logger: Logger = Logger.getInstance()
    // TODO figure out di management for multi-modules project

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var walletViewModel: WalletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mint_token_activity)

        WalletDi(application).getWalletComponent().inject(this)
        walletViewModel = ViewModelProvider(this, viewModelFactory).get(WalletViewModel::class.java)
//        val viewModel: WalletViewModel by viewModels()
//        viewModel.setRepository(applicationContext)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                walletViewModel.uiState.collect { it ->
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
                walletViewModel.balanceOf(getString(R.string.my_account))
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
                walletViewModel.mintToken(to, value, uri)
            }

        findViewById<Button>(R.id.getMyTokens)
            .setOnClickListener {
                val account = applicationContext.getString(R.string.my_account)
                walletViewModel.getTokensThatBelongsToMeNotConsumedNotExpired(account)
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
            }
    }

    fun testFunc(): Flow<Value> {
        return flow {
            val repo = WalletRepository(
                applicationContext,
                WalletModule(applicationContext)
                    .providesWeb3jHttpService(WalletModule(applicationContext).providesInterceptor())
            )
            val result = repo.getOffer("12")
            emit(result)
        }
            .flowOn(Dispatchers.IO)
    }
}