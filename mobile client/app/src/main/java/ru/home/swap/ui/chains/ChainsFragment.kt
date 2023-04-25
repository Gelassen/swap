package ru.home.swap.ui.chains

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.databinding.ChainsFragmentBinding
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.wallet.model.ITransaction
import javax.inject.Inject

class ChainsFragment: BaseFragment(), ChainsAdapter.ClickListener {

    private lateinit var binding: ChainsFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ChainsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ChainsViewModel::class.java)
        binding = ChainsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txList.adapter = ChainsAdapter(
            this,
            mainDispatcher = Dispatchers.Main,
            workerDispatcher = Dispatchers.IO)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.uiState
                        .collect{ it ->
                            if (it.pagedData == null) return@collect
                            (binding.txList.adapter as ChainsAdapter).submitData(it.pagedData)
                        }
                }
                launch { viewModel.getPersonProfile() }
                launch { viewModel.loadCachedByPage() }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { viewModel.fetchAggregatedMatches() }
            }
        }
    }

    override fun onItemClick(item: ITransaction) {
        TODO("Not yet implemented")
    }
}