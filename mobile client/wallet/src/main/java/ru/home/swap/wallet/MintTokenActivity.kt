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
                walletViewModel.registerUserOnSwapMarket(FIRST_USER)
            }

        findViewById<Button>(R.id.registerSecondUser)
            .setOnClickListener {
                walletViewModel.registerUserOnSwapMarket(SECOND_USER)
            }

        findViewById<Button>(R.id.approveTokenManager)
            .setOnClickListener {
                val swapChainAddress = getString(R.string.swap_chain_contract_address)
                walletViewModel.approveTokenManager(swapChainAddress)
            }

        LoggerFactory.getLogger(HttpService::class.java).isDebugEnabled

        findViewById<Button>(R.id.checkBalance)
            .setOnClickListener {
                walletViewModel.balanceOf(FIRST_USER/*getString(R.string.my_account)*/)
            }

        findViewById<Button>(R.id.mintToken)
            .setOnClickListener {
                val to = FIRST_USER/*getString(R.string.my_account)*/
                val value = Value(
                    FIRST_USER_OFFER,
                    BigInteger.valueOf(1665158348220),
                    BigInteger.valueOf(1669758348220),
                    false,
                    BigInteger.valueOf(0)
                )
                val uri = "https://gelassen.github.io/blog/"
                walletViewModel.mintToken(to, value, uri)
            }

        findViewById<Button>(R.id.mintTokenSecondUser)
            .setOnClickListener {
                val to = SECOND_USER/*getString(R.string.my_account)*/
                val value = Value(
                    SECOND_USER_OFFER,
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
                val account = FIRST_USER//applicationContext.getString(R.string.my_account)
                walletViewModel.getTokenIdsForUser(account)
            }

        findViewById<Button>(R.id.getMyTokensSecondUser)
            .setOnClickListener {
                val account = SECOND_USER//applicationContext.getString(R.string.my_account)
                walletViewModel.getTokenIdsForUser(account)
            }

        findViewById<Button>(R.id.approveSwap)
            .setOnClickListener {
                val userFirstTokenId = BigInteger.valueOf(0) // TODO define me
                val userSecondTokenId = BigInteger.valueOf(1) // TODO define me
                val matchSubj = Match(
                    userFirst = FIRST_USER.lowercase(),
                    valueOfFirstUser = userFirstTokenId,
                    userSecond = SECOND_USER.lowercase(),
                    valueOfSecondUser = userSecondTokenId,
                    approvedByFirstUser = false,
                    approvedBySecondUser = false
                )
                logger.d("Match object passed on approve call: $matchSubj")
                walletViewModel.approveSwap(matchSubj)
            }

        findViewById<Button>(R.id.registerDemandFirstUser)
            .setOnClickListener {
                val demandOfFirstUser = SECOND_USER_OFFER
                walletViewModel.registerDemand(FIRST_USER, demandOfFirstUser)
            }

        findViewById<Button>(R.id.registerDemandSecondUser)
            .setOnClickListener {
                val demandOfSecondUser = FIRST_USER_OFFER
                walletViewModel.registerDemand(SECOND_USER, demandOfSecondUser)
            }

        // TODO test registerDemand() and approveSwap(), also check a whole flow (don't forget insert token ids!)
    }

    companion object {
        const val FIRST_USER = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"
        const val FIRST_USER_OFFER = "Consulting"
        const val SECOND_USER = "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"
        const val SECOND_USER_OFFER = "Farmer products"
    }

}