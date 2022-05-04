package ru.home.swap

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import ru.home.swap.network.IApi
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import ru.home.swap.ui.profile.ProfileViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelUnitTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    lateinit var subject: ProfileViewModel

    private lateinit var autoCloseable: AutoCloseable

    private lateinit var repository: PersonRepository

    private val context: Context = mock()

    private val api: IApi = mock()

    private val cache: Cache = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        autoCloseable = MockitoAnnotations.openMocks(this)
        repository = PersonRepository(api, cache, context)

        subject = ProfileViewModel(repository, context)

        Mockito.`when`(context.getString(any()))
            .thenReturn("https://honeypot.com")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
        autoCloseable.close()
    }

    @Test
    fun `on get default state items`(): TestResult {
        return runTest {
            val originModel = subject.state.value

            Assert.assertEquals(originModel, subject.state.value)
        }
    }

    @Test
    fun `on add offer see change in model`(): TestResult {
        return runTest {
            val originModel = subject.state.value

            subject.proposal.set("New item")
            subject.addOffer()

            Assert.assertNotEquals(originModel, subject.state.value)
        }
    }

    @Test
    fun `on add offer flow has emit only once`(): TestResult {
        return runTest {
            val ref = subject.uiState

            subject.proposal.set("New item")
            subject.addOffer()

            Assert.assertNotEquals(1, subject.state.count())
        }
    }

    @Test
    fun `on add two offers there are two items in state`(): TestResult {
        return runTest {
            subject.proposal.set("New item")
            subject.addOffer()
            subject.proposal.set("Another item")
            subject.addOffer()

            Assert.assertEquals(2, subject.state.value.offers.count())
        }
    }

    @Test
    fun `on add three items and remove one there are two items in state`(): TestResult {
        return runTest {
            subject.proposal.set("New item")
            subject.addOffer()
            subject.proposal.set("Another item")
            subject.addOffer()
            subject.proposal.set("Third item")
            subject.addOffer()
            subject.removeOffer(subject.state.value.offers.get(0))

            Assert.assertEquals(2, subject.state.value.offers.count())
            Assert.assertEquals("Another item", subject.state.value.offers.first().title)
        }
    }

    @Test
    fun `on add four items there are four items in state`(): TestResult {
        return runTest {
            subject.proposal.set("New item")
            subject.addOffer()
            subject.proposal.set("Another item")
            subject.addOffer()
            subject.proposal.set("Third item")
            subject.addOffer()
            subject.proposal.set("Fourth item")
            subject.addOffer()

            Assert.assertEquals(4, subject.state.value.offers.count())
            Assert.assertEquals(4, subject.uiState.value.offers.count())
        }
    }

}