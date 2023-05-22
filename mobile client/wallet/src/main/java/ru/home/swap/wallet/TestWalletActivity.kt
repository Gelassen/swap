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
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.wallet.contract.Match
import java.math.BigInteger
import javax.inject.Inject

class TestWalletActivity: AppCompatActivity() {

    private val logger: Logger = Logger.getInstance()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var walletViewModel: WalletViewModel

    lateinit var personProfile: PersonProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mint_token_activity)

        WalletDi(application).getWalletComponent().inject(this)
        walletViewModel = ViewModelProvider(this, viewModelFactory).get(WalletViewModel::class.java)

        personProfile = PersonProfile(userWalletAddress = getString(R.string.first_account))

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
                val personProfile = PersonProfile(userWalletAddress = getString(R.string.first_account))
                walletViewModel.registerUserOnSwapMarket(FIRST_USER, personProfile = personProfile)
            }

        findViewById<Button>(R.id.registerSecondUser)
            .setOnClickListener {
                val personProfile = PersonProfile(userWalletAddress = getString(R.string.second_account))
                walletViewModel.registerUserOnSwapMarket(SECOND_USER, personProfile)
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
                val matchSubj = getMatchObj()
                logger.d("Match object passed on approve call: $matchSubj")
                walletViewModel.approveSwap(matchSubj)
            }

        findViewById<Button>(R.id.registerDemandFirstUser)
            .setOnClickListener {
                /**
                 * Register demand is deprecated since V2 smart contract
                 * */
            }

        findViewById<Button>(R.id.registerDemandSecondUser)
            .setOnClickListener {
                /**
                 * Register demand is deprecated since V2 smart contract
                 * */
            }

        findViewById<Button>(R.id.getMatches)
            .setOnClickListener {
                walletViewModel.getMatchesForProfile(FIRST_USER, SECOND_USER)
            }

        findViewById<Button>(R.id.swap)
            .setOnClickListener {
                val subj = getMatchObj()
                walletViewModel.swap(subj)
            }
    }

    private fun getMatchObj() : Match {
        val userFirstTokenId = BigInteger.valueOf(0) // TODO define me
        val userSecondTokenId = BigInteger.valueOf(1) // TODO define me
        return Match(
            userFirst = FIRST_USER.lowercase(),
            valueOfFirstUser = userFirstTokenId,
            userSecond = SECOND_USER.lowercase(),
            valueOfSecondUser = userSecondTokenId,
            approvedByFirstUser = false,
            approvedBySecondUser = false
        )
    }

    companion object {
        const val FIRST_USER = "0x0A8b295B4266d8fEB55d46a96B31936FE265C01F"
        const val FIRST_USER_OFFER = "Consulting"
        const val SECOND_USER = "0xED337a9841aa5349ACe99931460E0443a199E746"
        const val SECOND_USER_OFFER = "Farmer products"
    }

}