package ru.home.swap.wallet

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wallet.R
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlockNumber
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Transfer
import org.web3j.utils.Convert

import ru.home.swap.core.App
import java.io.File
import java.math.BigDecimal
import java.security.Provider
import java.security.Security


@Deprecated(message = "MintTokenActivity is used for debug purpose")
class DebugActivity: AppCompatActivity() {

    private lateinit var credentials: Credentials
    private lateinit var walletDir: File
    private lateinit var web3: Web3j

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.wallet.R.layout.debug_activity);
        setupBouncyCastle()

        test()

        val debugView = findViewById<TextView>(com.example.wallet.R.id.debugInfo)
        debugView.text = getString(R.string.ethereum_api_endpoint)

        findViewById<Button>(com.example.wallet.R.id.setupWallet)
            .setOnClickListener { setExistingWallet() }

        findViewById<Button>(com.example.wallet.R.id.createWallet)
            .setOnClickListener { createWallet() }

        findViewById<Button>(com.example.wallet.R.id.getCredentials)
            .setOnClickListener { getWalletAddress() }

        findViewById<Button>(com.example.wallet.R.id.testTransaction)
            .setOnClickListener { testTx() }

        findViewById<Button>(com.example.wallet.R.id.testBalance)
            .setOnClickListener {
                val result = checkBalance()
                debugView.text = result.result
            }
    }

    fun temporaryBlockOfCodeForExperiment() {
    }

    fun test() {
        val testAPI = "https://rinkeby.infura.io/v3/ce67e157fc964d3bbf7ff1db09aa316a"
        web3 = Web3j.build(HttpService(testAPI))
        try {
            val clientVersion = web3.web3ClientVersion().sendAsync().get()
            if (!clientVersion.hasError()) {
                //Connected
                Log.d(App.WALLET, "Connected to node: " + clientVersion.result)
            } else {
                //Show Error
                Log.e(App.WALLET, "Failed to connect to the node: " + clientVersion.result)
            }
        } catch (e: Exception) {
            //Show Error
            Log.e(App.WALLET, "Failed to connect", e)
        }
    }

    // Send tx successfully: 0xa5a235b1db98834e2bd7c7fce3e1dcb8c699b036f19f0fa35747d2704cff4acd
    fun setExistingWallet() {
        walletDir = File("/data/user/0/ru.home.swap.experiment/files/UTC--2022-09-16T09-54-53.936000000Z--07f9a0726448a02d1e007e75f5829ca7216dd67c.json")
        if (!walletDir.exists()) {
            throw IllegalStateException("This swap doesn't exist. Did you hardcoded correct file URI?")
        }
    }

    fun createWallet() {
        val password = "Swap42"
        val walletPath = filesDir.absolutePath
        walletDir = File(walletPath)

        try {
            val fileName = WalletUtils.generateLightNewWalletFile(password, walletDir)
            walletDir = File("$walletPath/$fileName")
            Log.d(App.WALLET, "New swap is created ${walletDir.absolutePath}")
        } catch (e: java.lang.Exception) {
            //Display an Error
            Log.e(App.WALLET, "Failed to create swap", e)
        }
    }

    private fun setupBouncyCastle() {
        val provider: Provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return

        if (provider.javaClass.equals(BouncyCastleProvider::class.java)) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun getWalletAddress() {
        try {
            credentials = WalletUtils.loadCredentials("Swap42", walletDir)
            Toast.makeText(this, "Your address is " + credentials.getAddress(), Toast.LENGTH_LONG)
                .show()
            Log.d(App.WALLET, "Wallet credentials obtained. Wallet's address ${credentials.address}")
        } catch (e: java.lang.Exception) {
            //Show Error
            Log.e(App.WALLET, "Failed to obtain swap credentials", e)
        }
    }

    private fun testTx() {
        try {
            val receipt = Transfer.sendFunds(
                web3,
                credentials,
                credentials.address,
                BigDecimal(100000),
                Convert.Unit.WEI
            ).sendAsync().get()
            Toast.makeText(
                this,
                "Transaction complete: " + receipt.transactionHash,
                Toast.LENGTH_LONG
            ).show()
            Log.d(App.WALLET, "Send tx successfully: ${receipt.transactionHash}")
        } catch (e: java.lang.Exception) {
            //Show Error
            Log.e(App.WALLET, "Failed to send tx", e)
        }
    }

    fun getBlockNumber(): EthBlockNumber {
        var result = EthBlockNumber()
        result = web3.ethBlockNumber()
            .sendAsync()
            .get()
        return result
    }

    private fun checkBalance(): EthGetBalance {
        val defaultBlockNumber = DefaultBlockParameter.valueOf("latest")
        val result = web3.ethGetBalance(credentials.address, defaultBlockNumber)
            .sendAsync()
            .get()
        Log.d(App.WALLET, "Wallet's balance ${result.balance}")
        return result
    }


}