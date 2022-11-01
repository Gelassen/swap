package com.example.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wallet.debug.Status
import com.example.wallet.debug.WalletViewModel
import com.example.wallet.debug.repository.WalletRepository
import com.example.wallet.rules.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
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
    var repository: WalletRepository = mock(WalletRepository::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        subj = WalletViewModel(repository)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `when balanceOf() is called with existing parameter model state is updated with correct value`() = runTest {
        val balance = 42L
        val balanceOfFlow = flow {
            emit(BigInteger.valueOf(balance))
        }
        `when`(repository.balanceOf(anyString())).thenReturn(balanceOfFlow)
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
}