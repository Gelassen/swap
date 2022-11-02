package com.example.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wallet.debug.Status
import com.example.wallet.debug.WalletViewModel
import com.example.wallet.debug.repository.StorageRepository
import com.example.wallet.debug.repository.WalletRepository
import com.example.wallet.rules.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.math.BigInteger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

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
    var cacheRepository: StorageRepository = mock(StorageRepository::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        subj = WalletViewModel(walletRepository, cacheRepository)
    }

    @Test
    fun addition_isCorrect() = runBlockingTest {
        val balance = 42L
        val balanceOfFlow = flow {
            emit(BigInteger.valueOf(balance))
        }
        Mockito.`when`(walletRepository.balanceOf(Mockito.anyString())).thenReturn(balanceOfFlow)
        mainCoroutineRule.dispatcher.pauseDispatcher()
        MatcherAssert.assertThat(
            "Model balance is not default",
            subj.uiState.value.wallet.getBalance().toInt() == 0
        )
        MatcherAssert.assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        MatcherAssert.assertThat("State is not default", subj.uiState.value.status == Status.NONE)

        subj.balanceOf("0x6f1d841afce211dAead45e6109895c20f8ee92f0")
        mainCoroutineRule.dispatcher.resumeDispatcher()

        MatcherAssert.assertThat(
            "Model balance is not updated with correct value",
            subj.uiState.value.wallet.getBalance().toLong() == balance
        )
        MatcherAssert.assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        MatcherAssert.assertThat(
            "State is set as BALANCE",
            subj.uiState.value.status == Status.BALANCE
        )
    }
}