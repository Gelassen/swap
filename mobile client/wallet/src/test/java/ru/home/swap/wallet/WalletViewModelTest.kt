package ru.home.swap.wallet

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.mockito.Mockito
import ru.home.swap.wallet.WalletViewModelTest.Const.FIRST_USER
import ru.home.swap.wallet.WalletViewModelTest.Const.FIRST_USER_OFFER
import ru.home.swap.wallet.WalletViewModelTest.Const.SECOND_USER
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.fakes.FakeStorageRepository
import ru.home.swap.wallet.fakes.FakeWalletRepository
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.rules.MainCoroutineRule
import ru.home.swap.wallet.storage.model.ServerTransaction
import java.math.BigInteger

import ru.home.swap.core.model.PersonProfile
import ru.home.swap.wallet.storage.model.TxStatus

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("After migration on-chain work to WorkManager worker, this test suite should be moved " +
        "to androidTest package. There was not a quick way to quickly fix all errors after such " +
        "move and this test suite was left here. " +
        "There was a successful attempt to mock the WorkManager, but it also means to lose all " +
        "control on granular chain test responses which lead to rewrite the most of the test suite. " +
        "" +
        "For now the test suite is disabled. All considered options are tracked in comments in " +
        "case future requirements will rise this scope of work back to the top.")
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

    /**
     * Implement fake WorkManager is not possible. It uses package scoped
     * classes which are forbidden for inheritance with sealed-like behaviour
     * */
//    @Mock
    lateinit var workManager: WorkManager
//    @Mock
    lateinit var mockedLiveData: LiveData<WorkInfo>

    private lateinit var subj: WalletViewModel
    private val fakeWalletRepository = FakeWalletRepository()
    private val fakeStorageRepository = FakeStorageRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
//        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
//        MockitoAnnotations.openMocks(this);

        workManager = Mockito.mock(WorkManager::class.java)
        /*mockedLiveData = mock<LiveData<WorkInfo>>()*/
        mockedLiveData = MutableLiveData<WorkInfo>()

        val mockedApp = Mockito.mock(Application::class.java)
        subj = WalletViewModel(
            mockedApp,
            fakeWalletRepository,
            fakeStorageRepository,
            workManager,
            StandardTestDispatcher()
        )
    }

    @After
    fun tearDown() {
        fakeStorageRepository.reset()
    }

    // haven't found yet way to run unit test on android, test suite moved back to the test package
    // TODO consider simply mock Worker as you testing view model behaviour

    @Test
    fun `on balanceOf with existing parameter model state is updated with correct value`() = runTest {
        fakeWalletRepository.setPositiveBalanceOfResponse()
        assertThat("Model balance is not default", subj.uiState.value.balance.toInt() == 0)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is not default", subj.uiState.value.status == Status.NONE)

        subj.balanceOf("0x6f1d841afce211dAead45e6109895c20f8ee92f0")
        advanceUntilIdle()

        assertThat("Model balance is not updated with correct value", subj.uiState.value.balance.toLong() == 42L)
        assertThat("Errors queue is not empty", subj.uiState.value.errors.isEmpty())
        assertThat("State is not set as BALANCE", subj.uiState.value.status == Status.BALANCE)
    }

    /**
     * Kotlin extensions are recognised as Java static methods.
     *
     * Mockito since v5 has built-in support of such functions. Before v5 it could be enabled manually.
     *
     * However, by using mockito I wasn't able to test asFlow() extension for a livedata.
     *
     * Mockk is an another alternative for a such scenario. The extra attention should be payed
     * for @file:JvmName("FlowLiveDataConversions") as it overrides package name which should be
     * defined in mockkStatic(<full path>).
     * */
    /*@Test
    fun `on mintToken() with valid data the first time, pending tx state is increased and cache has new record`() = runTest {
        val flow = flow {
            val workInfo: WorkInfo
            workInfo = WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, Data.EMPTY, emptyList(), Data.EMPTY, 0, 0)
            emit(workInfo)
        }
        mockkStatic("androidx.lifecycle.FlowLiveDataConversions")
        every {
            mockedLiveData.asFlow()
        } returns flow
        `when`(workManager.getWorkInfoByIdLiveData(any()))
            .thenReturn(mockedLiveData)

        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
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
        fakeWalletRepository.swapValueResponse.setPositiveMintTokenResponse()

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        *//*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        * assertThat("Model should have a single pending tx, but has $pendingTxFinalState", pendingTxFinalState == 1)
        * *//*

        assertThat("Model should not have errors", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        // cleanup
        jobCacheInitialStatus.cancel()
    }*/

    @Test
    fun `on mintToken() with valid data the first time, pending tx state is increased and cache has new record`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
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
        fakeWalletRepository.swapValueResponse.setPositiveMintTokenResponse()

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        /*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        * assertThat("Model should have a single pending tx, but has $pendingTxFinalState", pendingTxFinalState == 1)
        * */
        assertThat("Model should not have errors", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        // cleanup
        jobCacheInitialStatus.cancel()
    }

    @Test
    fun `on mintToken() with negative response from server, pending tx state is increased and cache has new record with both negative state`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
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
        fakeWalletRepository.swapValueResponse.setNegativeMintTokenResponse()

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        /*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        * assertThat("Model should have a single pending tx, but has $pendingTxFinalState", pendingTxFinalState == 1)
        * */

        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat(
            "Cache should have record with 'reverted' status, but has ${cacheFinalState.get(0).first.status}",
            cacheFinalState.get(0).first.status.equals("reverted")
        )
        /*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * assertThat(
            "Model should have a pending tx with 'reverted' status, but has ${subj.uiState.value.pendingTx.get(0).status}",
            subj.uiState.value.pendingTx.get(0).status.equals("reverted")
        )
        * */

        assertThat(
            "Error should have specific revert cause: ${subj.uiState.value.errors.first()}",
            subj.uiState.value.errors.first().equals("Reverted cause: Artificially made negative response caused by 'revert'")
        )
        // cleanup
        jobCacheInitialStatus.cancel()
    }

    @Test
    fun `on mintToken() with exception, pending tx state is increased and cache has new record with both negative state`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        val jobCacheInitialStatus = launch(UnconfinedTestDispatcher(mainCoroutineRule.dispatcher.scheduler)) {
            fakeStorageRepository.getAllChainTransactions()
                .collect { it ->
                    cacheInitialStatus = it
                }
        }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        // prepare dataset
        val to = FIRST_USER
        val value = Value(
            FIRST_USER_OFFER,
            BigInteger.valueOf(1665158348220),
            BigInteger.valueOf(1669758348220),
            false,
            BigInteger.valueOf(0)
        )
        val uri = "https://gelassen.github.io/blog/"
        fakeWalletRepository.swapValueResponse.setExceptionErrorMintTokenResponse()

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        /*
        * val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * assertThat("Model should have a single pending tx $pendingTxFinalState", pendingTxFinalState == 1)
        * */

        assertThat("Model should have a single error", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat(
            "Cache should have record with 'exception' status, but has ${cacheFinalState.get(0).first.status}",
            cacheFinalState.get(0).first.status.equals("exception")
        )

        /*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * assertThat(
            "Model should have a pending tx with 'exception' status, but has ${subj.uiState.value.pendingTx.get(0).status}",
            subj.uiState.value.pendingTx.get(0).status.equals("exception")
        )
        * */

        assertThat(
            "Error should have specific revert cause: ${subj.uiState.value.errors.first()}",
            subj.uiState.value.errors.first().equals("Exception: Artificially made negative response caused by 'revert'")
        )
        // cleanup
        jobCacheInitialStatus.cancel()
    }

    @Test
    fun `on mintToken() with error message, pending tx state is increased and cache has new record with both negative state`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        val jobCacheInitialStatus = launch(UnconfinedTestDispatcher(mainCoroutineRule.dispatcher.scheduler)) {
            fakeStorageRepository.getAllChainTransactions()
                .collect { it ->
                    cacheInitialStatus = it
                }
        }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        // prepare dataset
        val to = FIRST_USER
        val value = Value(
            FIRST_USER_OFFER,
            BigInteger.valueOf(1665158348220),
            BigInteger.valueOf(1669758348220),
            false,
            BigInteger.valueOf(0)
        )
        val uri = "https://gelassen.github.io/blog/"
        fakeWalletRepository.swapValueResponse.setErrorMintTokenResponse()

        subj.mintToken(to, value, uri)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        /*
        * val pendingTxFinalState = subj.uiState.value.pendingTx.count()
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * assertThat("Model should have a single pending tx $pendingTxFinalState", pendingTxFinalState == 1)
        * */

        assertThat("Model should have a single error", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat(
            "Cache should have record with 'exception' status, but has ${cacheFinalState.get(0).first.status}",
            cacheFinalState.get(0).first.status.equals("exception")
        )
        /*
        * Known issue with Flow in init {} block is not triggered from the test on the table changes
        * assertThat(
            "Model should have a pending tx with 'exception' status, but has ${subj.uiState.value.pendingTx.get(0).status}",
            subj.uiState.value.pendingTx.get(0).status.equals("exception")
        )
        * */

        assertThat(
            "Error should have specific revert cause: ${subj.uiState.value.errors.first()}",
            subj.uiState.value.errors.first().equals("Exception: Artificially made negative response caused by 'revert'")
        )
        // cleanup
        jobCacheInitialStatus.cancel()
    }

    @Test
    fun `On registerUserOnSwapMarket() with valid data first time, pending tx state is increased and cache has a new record`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setPositiveRegisterUserResponse()

        val user = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F0000"
        val personProfile = PersonProfile()
        subj.registerUserOnSwapMarket(user, personProfile)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should not have errors, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `mined` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_MINED)
    }

    @Test
    fun `On registerUserOnSwapMarket() with valid data second time, pending tx state is increased and cache has a new record, but has exception status and error list has a single record`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setPositiveRegisterUserResponse()
        val user = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(user, PersonProfile())
        advanceUntilIdle()
        fakeWalletRepository.swapValueResponse.setExceptionErrorRegisterUserResponse()

        subj.registerUserOnSwapMarket(user, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have two records, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 2)
        assertThat("Cache should have a record with `mined` status and record with `exception` status, " +
                "but it has ${cacheFinalState.get(0).first.status} and ${cacheFinalState.get(1).first.status}",
            cacheFinalState.get(0).first.status == TxStatus.TX_MINED
                        && cacheFinalState.get(1).first.status.equals(TxStatus.TX_EXCEPTION)
        )
        assertThat(
            "Error should have defined message, but has ${subj.uiState.value.errors.get(0)}",
            subj.uiState.value.errors.get(0).equals("Exception: Transaction 0xe48d2704a0c3ec9d86288736709fb2cf0d3fcc4b1a0797f136ad59ebc83445b9 has failed with status: 0x0. Gas used: 33112. Revert reason: 'execution reverted: User already registered.'.")
        )
    }

    @Test
    fun `On registerUserOnMarket() with negative response, pending tx state is increased and cache has a new record, but has reverted status and error list has a single record`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setNegativeRegisterUserResponse()

        val user = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(user, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `reverted` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_REVERTED)
        assertThat("Error should have defined message, but has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0).equals("Reverted cause: Artificially made negative response"))
    }

    @Test
    fun `On registerUserOnMarket() with error response, pending tx state is increased and cache has a single record, both has exception status and errors has a single item`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setErrorRegisterUserResponse()

        val user = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(user, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
        assertThat("Error should have defined message, but has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0).equals("Exception: Artificially made negative response"))
    }

    @Test
    fun `On approveTokenManger() with valid params, pending tx state is increased, cache has a new record, both has mined tx status, errors list is empty`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setPositiveApproveTokenManagerResponse()

        val swapMarket = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(swapMarket, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should not have errors, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `mined` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_MINED)
    }

    @Test
    fun `On approveTokenManger() with non-owner sender, pending tx state is increased, cache has a new record, both has exception tx status, errors list has a single item`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setExceptionErrorApproveTokenManagerResponse()

        val swapMarket = "0x000000005c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(swapMarket, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
    }

    @Test
    fun `On approveTokenManger() with error response, pending tx state is increased, cache has a new record, both has exception tx status, errors list has a single item`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setErrorApproveTokenManagerResponse()

        val swapMarket = "0x000000005c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(swapMarket, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
    }

    @Test
    fun `On approveTokenManger() with negative response, pending tx state is increased, cache has a new record, both has reverted tx status, errors list has a single item`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setNegativeApproveTokenManagerResponse()

        val swapMarket = "0x000000005c80db6e8FCc042f0cC54a298F8F0000"
        subj.registerUserOnSwapMarket(swapMarket, PersonProfile())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `reverted` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_REVERTED)
    }

    @Test
    fun `on approveSwap() with valid params, pending tx state is increased, cache has a new record, both has 'mined' tx status, errors list is empty`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setPositiveApproveSwapResponse()

        val obj = getMatchObj()
        subj.approveSwap(obj)
        advanceUntilIdle()
        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should not have errors, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `mined` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_MINED)

    }

    @Test
    fun `on approveSwap() with negative response, pending tx state is increased, cache has a new record, both has 'reverted' tx status, errors list has single error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setNegativeApproveSwapResponse()

        val obj = getMatchObj()
        subj.approveSwap(obj)
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `reverted` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_REVERTED)
    }

    @Test
    fun `on approveSwap() with error response, pending tx state is increased, cache has a new record, both has 'exception' tx status, errors list has single error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setErrorApproveSwapResponse()

        subj.approveSwap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
    }

    @Test
    fun `on approveSwap() with exception response, pending tx state is increased, cache has a new record, both has 'exception' tx status, errors list has single error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setExceptionApproveSwapResponse()

        subj.approveSwap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
        assertThat("Model should have a single error with defined message, but it has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0) == "Exception: Match item is not found. Did you pass correct match object?")
    }

    @Test
    @Ignore("Since SwapChainV2.sol registerDemand() is not supported")
    fun `On registerDemand() with valid params, pending tx state is increased, cache has a new record, both has a 'mined' tx status, errors list is empty`() = runTest {
        assertThat("Model should not have pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model should not have errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        // TOOD not completed
    }

    @Test
    fun `On swap() with valid param, pending tx state is increased, cache has a new record, both has a 'mined' status, errors list is empty`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setPositiveSwapResponse()

        subj.swap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should not have errors, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.isEmpty())
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `mined` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_MINED)
    }

    @Test
    fun `On swap() with negative response, pending tx state is increased, cache has a new record, both has a 'reverted' status, errors list has an error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setNegativeSwapResponse()

        subj.swap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `reverted` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_REVERTED)
        assertThat("Cache should have a single error with defined message, but it has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0) == "Reverted cause: Artificially made negative response")
    }

    @Test
    fun `On swap() with error response, pending tx state is increased, cache has a new record, both has an 'exception' status, errors list has an error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setErrorSwapResponse()

        subj.swap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
        assertThat("Model should have a single error with defined message, but it has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0) == "Exception: Artificially made negative response")
    }

    @Test
    fun `On swap() with exception response, pending tx state is increased, cache has a new record, both has an 'exception' status, errors list has an error`() = runTest {
        assertThat("Model has some pending tx", subj.uiState.value.pendingTx.isEmpty())
        assertThat("Model has some errors", subj.uiState.value.errors.isEmpty())
        var cacheInitialStatus = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect { it ->
                cacheInitialStatus = it
            }
        advanceUntilIdle()
        assertThat("Cache is non empty with value count ${cacheInitialStatus.count()}", cacheInitialStatus.isEmpty())
        fakeWalletRepository.swapValueResponse.setExceptionSwapResponse()

        subj.swap(getMatchObj())
        advanceUntilIdle()

        var cacheFinalState = emptyList<Pair<ITransaction, ServerTransaction>>()
        fakeStorageRepository.getAllChainTransactions()
            .collect {
                cacheFinalState = it
            }
        advanceUntilIdle()
        assertThat("Model should have a single error, but it has ${subj.uiState.value.errors.count()}", subj.uiState.value.errors.count() == 1)
        assertThat("Cache should have a single record, but it has ${cacheFinalState.count()}", cacheFinalState.count() == 1)
        assertThat("Cache should have a record with `exception` status, but it has ${cacheFinalState.get(0).first.status}", cacheFinalState.get(0).first.status == TxStatus.TX_EXCEPTION)
        assertThat("Model should have a single error with defined message, but it has ${subj.uiState.value.errors.get(0)}", subj.uiState.value.errors.get(0) == "Exception: Transaction 0x3292880f1157ff54c168cfa3b0485c1933c41263d76c90e9ba9fac26948f832a has failed with status: 0x0. Gas used: 48124. Revert reason: 'execution reverted: Tokens should not be already consumed.'.")
    }

/*    @Test
    fun `balanceOf() with turbine lib test`() = runTest {
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

    private fun getMatchObj() : Match {
        val userFirstTokenId = BigInteger.valueOf(0)
        val userSecondTokenId = BigInteger.valueOf(1)
        return Match(
            userFirst = FIRST_USER.lowercase(),
            valueOfFirstUser = userFirstTokenId,
            userSecond = SECOND_USER.lowercase(),
            valueOfSecondUser = userSecondTokenId,
            approvedByFirstUser = false,
            approvedBySecondUser = false
        )
    }

    object Const {
        const val FIRST_USER = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"
        const val FIRST_USER_OFFER = "Consulting"
        const val SECOND_USER = "0x52E7400Ba1B956B11394a5045F8BC3682792E1AC"
        const val SECOND_USER_OFFER = "Farmer products"
    }
}