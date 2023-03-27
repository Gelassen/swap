package ru.home.swap

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.model.Service
import ru.home.swap.ui.profile.ProfileV2ViewModel
import ru.home.swap.wallet.model.MintTransaction
import javax.inject.Inject

class TestActivity: AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileV2ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
        AndroidInjection.inject(this)

        /**
         * By not discovered yet cause, WHERE clause on sql query layer
         * doesn't work properly for loadAllTransactions(); querying all
         * transactions and filtration on the repository layer has solved
         * the issue
         * */

        // TODO check influence of FK on search result

        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileV2ViewModel::class.java)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.debugGetAllChainTx() }
                launch { viewModel.loadAllFromCache() }
            }
        }

        findViewById<Button>(R.id.addNew)
            .setOnClickListener {
                lifecycleScope.launch {
                    val tx = MintTransaction()
                    val serverItem = Service()
                    viewModel.createARecord(tx, serverItem)
                }
            }

        findViewById<Button>(R.id.updateRecord)
            .setOnClickListener {
                lifecycleScope.launch {
                    viewModel.updateARecord()
                }
            }
    }
}