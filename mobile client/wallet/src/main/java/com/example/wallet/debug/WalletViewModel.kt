package com.example.wallet.debug

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.model.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import ru.home.swap.core.logger.Logger
import java.lang.Exception
import java.lang.RuntimeException
import java.math.BigInteger

data class Model(
    var privateKey: String = "", // TODO refactor this dev mode solution
    val wallet: Wallet = Wallet(),
    val status: Status = Status.NONE,
    val errors: List<String> = emptyList()
)
enum class Status {
    NONE, BALANCE, MINT_TOKEN
}
class WalletViewModel(): ViewModel() {

    private val logger: Logger = Logger.getInstance()
    private lateinit var repository: WalletRepository

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun setRepository(context: Context) {
        repository = WalletRepository(context)
    }

    fun balanceOf(owner: String) {
        logger.d("[start] balanceOf()")
        viewModelScope.launch {
            repository.balanceOf(owner)
                .flowOn(Dispatchers.IO)
                .collect { value ->
                    logger.d("collect get balance result")
                    state.update {
                        it.wallet.setBalance(value)
                        it.copy(wallet = it.wallet, status = Status.BALANCE)
                    }
                }
        }
        logger.d("[end] balanceOf()")
    }

    fun mintToken(to: String, value: Value, uri: String) {
        logger.d("[start] mintToken()")
        viewModelScope.launch {
            repository.mintToken(to, value, uri)
                .flowOn(Dispatchers.IO)
//                .catch {
//                    logger.e(it.stackTrace.toString(), RuntimeException())
//                    logger.e("Failed to mint token", RuntimeException())
//                }
                .collect {
                    // TODO complete me
                    logger.d(it.toString())
                    state.update {
                        it.copy(status = Status.MINT_TOKEN)
                    }
                }
        }
        logger.d("[end] mintToken()")
    }
}