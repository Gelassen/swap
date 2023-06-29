package ru.home.swap.ui.demands

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.databinding.DemandsFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.contacts.ContactsFragment
import ru.home.swap.ui.offers.OffersAdapter
import javax.inject.Inject

class DemandsFragment: BaseFragment(), DemandsAdapter.IListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: DemandsViewModel
    private lateinit var binding: DemandsFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(DemandsViewModel::class.java)

        binding = DemandsFragmentBinding.inflate(inflater, container, false)
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

    override fun onItemClick(item: Service) {
//        Log.e(App.TAG, "On item click ${item.userSecondServiceTitle}")
        val bundle = bundleOf(ContactsFragment.Params.EXTRA_SERVICE_ID to item.id) // FIXME just a stub due refactoring, reimplemenet it
        findNavController().navigate(R.id.action_offersFragment_to_contactsFragment, bundle)
    }

    private fun setupList() {
        binding.demandsList.adapter = DemandsAdapter(this)
        binding.demandsList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.demandsList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                (binding.demandsList.layoutManager as LinearLayoutManager).getOrientation()
            )
        )
    }

    private fun listenUpdates() {
        lifecycleScope.launch {
            viewModel.uiState.collect { it ->
                Log.d(App.PAGING, "[offers] get update from viewmodel ${it.pagingData}")
                binding.progressIndicator.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                if (it.errors.isNotEmpty()) {
                    showErrorDialog(it.errors.get(0))
                }
                if (it.pagingData != null) {
                    (binding.demandsList.adapter as DemandsAdapter).submitData(it.pagingData)
                }
                binding.noContent.visibility = if (binding.demandsList.adapter?.itemCount == 0) View.VISIBLE else View.GONE
            }
        }
        lifecycleScope.launch {
            (binding.demandsList.adapter as DemandsAdapter).loadStateFlow.collectLatest { loadState ->
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        // no op
                    }
                    is LoadState.Error -> {
                        viewModel.addError((loadState.refresh as LoadState.Error).error.localizedMessage!!)
                    }
                    is LoadState.NotLoading -> {
                        val isNoContent = binding.demandsList.adapter!!.itemCount == 0
                        binding.noContent.visibility = if (isNoContent) View.VISIBLE else View.GONE
                        binding.progressIndicator.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun fetchOffers() {
        lifecycleScope.launch {
            viewModel.fetchDemands()
        }
    }
}