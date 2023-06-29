package ru.home.swap.ui.chains

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.databinding.ChainsFragmentBinding
import ru.home.swap.ui.common.BaseFragment
import javax.inject.Inject

class ChainsFragment: BaseFragment(), ChainsAdapter.IListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ChainsViewModel
    private lateinit var binding: ChainsFragmentBinding

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
        binding.lifecycleOwner = this

        setupList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchOffers()
        listenUpdates()
        Log.d(App.TAG, "[offers] onViewCreated()")
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }

    override fun onPositiveClick() {
        super.onPositiveClick()
        viewModel.removeShownError()
    }

    override fun onItemClick(item: SwapMatch) {
        Log.e(App.TAG, "On item click ${item.userSecondServiceTitle}")
    }

    private fun setupList() {
        binding.txList.adapter = ChainsAdapter(this)
        binding.txList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.txList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                (binding.txList.layoutManager as LinearLayoutManager).getOrientation()
            )
        )
    }

    private fun listenUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.uiState.collect { it ->
                        Log.d(App.PAGING, "[offers] get update from viewmodel ${it.pagingData}")
//                        binding.progressIndicator.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                        if (it.errors.isNotEmpty()) { showErrorDialog(it.errors.get(0)) }
                        if (it.pagingData != null) {
//                            throw UnsupportedOperationException("Not supported yet for new type ${it.pagingData}")
                            (binding.txList.adapter as ChainsAdapter).setProfileId(it.profile?.id)
                            (binding.txList.adapter as ChainsAdapter).submitData(it.pagingData)
                        }
                        binding.noContent.visibility = if (binding.txList.adapter!!.itemCount == 0) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    (binding.txList.adapter as ChainsAdapter).loadStateFlow.collectLatest { loadState ->
                        when (loadState.refresh) {
                            is LoadState.Loading -> {
                                // no op
                            }
                            is LoadState.Error -> {
                                viewModel.addError((loadState.refresh as LoadState.Error).error.localizedMessage!!)
                            }
                            is LoadState.NotLoading -> {
                                val isNoContent = binding.txList.adapter!!.itemCount == 0
                                binding.noContent.visibility =
                                    if (isNoContent) View.VISIBLE else View.GONE
//                                binding.progressIndicator.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchOffers() {
        lifecycleScope.launch {
            viewModel.fetchOffers()
        }
    }
}