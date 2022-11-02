package com.example.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wallet.debug.Status
import com.example.wallet.debug.WalletViewModel
import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.repository.StorageRepository
import com.example.wallet.debug.repository.WalletRepository
import com.example.wallet.rules.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.Assert.assertEquals
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.network.Response
import java.math.BigInteger

@OptIn(ExperimentalCoroutinesApi::class)
internal class WalletViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var subj: WalletViewModel

    @Mock
    var walletRepository: WalletRepository = mock(WalletRepository::class.java)
    @Mock
    val cacheRepository: StorageRepository = mock(StorageRepository::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        subj = WalletViewModel(walletRepository, cacheRepository)
    }

    @After
    fun tearDown() {
        // no op
    }

    @Test
    fun `when balanceOf() is called with existing parameter model state is updated with correct value`() = runTest {
        val balance = 42L
        val balanceOfFlow = flow {
            emit(BigInteger.valueOf(balance))
        }
        `when`(walletRepository.balanceOf(anyString())).thenReturn(balanceOfFlow)
        mainCoroutineRule.dispatcher.pauseDispatcher()
        assertThat("Model balance is not default", subj.uiState.value.wallet.getBalance().toInt() == 0)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is not default", subj.uiState.value.status == Status.NONE)

        subj.balanceOf("0x6f1d841afce211dAead45e6109895c20f8ee92f0")
        mainCoroutineRule.dispatcher.resumeDispatcher()

        assertThat("Model balance is not updated with correct value", subj.uiState.value.wallet.getBalance().toLong() == balance)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is set as BALANCE", subj.uiState.value.status == Status.BALANCE)
    }

    @Ignore("Have not fixed yet the issue with not triggering catch block on error in flow{}. Test case is wrote for FakeRepository")
    @Test
    fun `when mintToken() is called with correct values, timeout exception is returned and pending tx are updated with new value`() = runTest {
        val to = "0x6f1d841afce211dAead45e6109895c20f8ee92f0"
        val url = "https://google.com"
        val testValue = Value(
            "Software Development",
            BigInteger.valueOf(1000L),
            BigInteger.valueOf(2000L),
            false,
            BigInteger.valueOf(0)
        )

        subj.mintToken(to, testValue, url)

        assertThat(
            "There is no pending transaction after mint a new token with timeout error",
            subj.uiState.value.pendingTx.isNotEmpty()
        )
    }
}