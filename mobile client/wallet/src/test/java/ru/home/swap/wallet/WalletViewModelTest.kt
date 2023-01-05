package ru.home.swap.wallet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import ru.home.swap.wallet.WalletViewModelTest.Const.FIRST_USER
import ru.home.swap.wallet.WalletViewModelTest.Const.FIRST_USER_OFFER
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.fakes.FakeStorageRepository
import ru.home.swap.wallet.fakes.FakeWalletRepository
import ru.home.swap.wallet.model.Transaction
import ru.home.swap.wallet.rules.MainCoroutineRule
import java.math.BigInteger

@OptIn(ExperimentalCoroutinesApi::class)
internal class WalletViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    /*
    * You can customise Dispatcher behaviour in each test by adding:
    *   Dispatchers.setMain(StandardTestDispatcher())
    *   Dispatchers.resetMain()
    *
    * Keep in mind UnconfinedTestDispatcher() run all coroutines at once when
    * StandardTestDispatcher() allows more granular control over coroutines.
    * */
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var subj: WalletViewModel
    private val fakeWalletRepository = FakeWalletRepository()
    private val fakeStorageRepository = FakeStorageRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        subj = WalletViewModel(
            fakeWalletRepository,
            fakeStorageRepository,
            StandardTestDispatcher()
        )
    }

    @After
    fun tearDown() {
        fakeStorageRepository.reset()
    }

    @Test
    fun `when balanceOf() is called with existing parameter model state is updated with correct value`() = runTest {
        fakeWalletRepository.setPositiveBalanceOfResponse()
        assertThat("Model balance is not default", subj.uiState.value.wallet.getBalance().toInt() == 0)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is not default", subj.uiState.value.status == Status.NONE)

        subj.balanceOf("0x6f1d841afce211dAead45e6109895c20f8ee92f0")
        advanceUntilIdle()

        assertThat("Model balance is not updated with correct value", subj.uiState.value.wallet.getBalance().toLong() == 42L)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is not set as BALANCE", subj.uiState.value.status == Status.BALANCE)
    }

    @Test
    fun `when mintToken() is called with valid data the first time, pending tx state is increased and cache has new record`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Transaction>()
        val jobCacheInitialStatus = launch(UnconfinedTestDispatcher(mainCoroutineRule.dispatcher.scheduler)) {
            fakeStorageRepository.getAllChainTransactions()
                .collect { it ->
                    cacheInitialStatus = it
                }
        }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        val to = FIRST_USER
        val value = Value(
            FIRST_USER_OFFER,
            BigInteger.valueOf(1665158348220),
            BigInteger.valueOf(1669758348220),
            false,
            BigInteger.valueOf(0)
        )
        val uri = "https://gelassen.github.io/blog/"

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        assertThat("Model does not have a single pending tx $pendingTxFinalState", pendingTxFinalState == 1)
        assertThat("Model has some errors errors", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record", fakeStorageRepository.getAllChainTransactions().count() == 1)
        // cleanup
        jobCacheInitialStatus.cancel()
    }

/*    @Test
    fun `balanceOf() turbine test`() = runTest {
        fakeWalletRepository.setPositiveBalanceOfResponse()

        subj.uiState.test {
            val initialState = awaitItem()
            assertThat("Model balance is not default", initialState.wallet.getBalance().toInt() == 0)
            assertThat("Errors queue is not empty", initialState.errors.isEmpty())
            assertThat("State is not default", initialState.status == Status.NONE)

            subj.balanceOf("0x6f1d841afce211dAead45e6109895c20f8ee92f0")

            val finalState = awaitItem()
            assertThat("Model balance is not updated with correct value", finalState.wallet.getBalance().toLong() == 42L)
            assertThat("Errors queue is not empty", finalState.errors.isEmpty())
            assertThat("State is not set as BALANCE", finalState.status == Status.BALANCE)
        }
    }*/

/*    @Test
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
    }*/

    object Const {
        const val FIRST_USER = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"
        const val FIRST_USER_OFFER = "Consulting"
        const val SECOND_USER = "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"
        const val SECOND_USER_OFFER = "Farmer products"
    }
}