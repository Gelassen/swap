package ru.home.swap.wallet

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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
import org.slf4j.LoggerFactory
import org.web3j.protocol.http.HttpService
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.logger.Logger
import ru.home.swap.wallet.contract.Match
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


        findViewById<TextView>(R.id.infoView).text = getString(R.string.ethereum_api_endpoint)

        findViewById<Button>(R.id.registerUser)
            .setOnClickListener {
                val testUser = "0x62F8DC8a5c00006e000000000cC54a298F8F2FFd"
                walletViewModel.registerUserOnSwapMarket(testUser)
            }

        findViewById<Button>(R.id.approveTokenManager)
            .setOnClickListener {
                val swapChainAddress = getString(R.string.swap_chain_contract_address)
                walletViewModel.approveTokenManager(swapChainAddress)
            }

        val privateSwapChainNode1Account = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"

        LoggerFactory.getLogger(HttpService::class.java).isDebugEnabled

        findViewById<Button>(R.id.checkBalance)
            .setOnClickListener {
                walletViewModel.balanceOf(privateSwapChainNode1Account/*getString(R.string.my_account)*/)
            }

        findViewById<Button>(R.id.mintToken)
            .setOnClickListener {
                val to = privateSwapChainNode1Account/*getString(R.string.my_account)*/
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

        findViewById<Button>(R.id.approveSwap)
            .setOnClickListener {
                val userFirstTokenId = BigInteger.valueOf(0) // TODO define me
                val userSecondTokenId = BigInteger.valueOf(0) // TODO define me
                val matchSubj = Match(
                    userFirst = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd",
                    valueOfFirstUser = userFirstTokenId,
                    userSecond = "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC",
                    valueOfSecondUser = userSecondTokenId,
                    approvedByFirstUser = true,
                    approvedBySecondUser = true
                )
                walletViewModel.approveSwap(matchSubj)
            }

        findViewById<Button>(R.id.registerDemand)
            .setOnClickListener {
                val demandOfFirstUser = "Farmer products"
                walletViewModel.registerDemand(demandOfFirstUser)
            }
    }

}