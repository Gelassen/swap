package ru.home.swap.ui.demands

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.databinding.DemandsFragmentBinding
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.model.Service
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.offers.OffersAdapter
import ru.home.swap.ui.offers.OffersViewModel
import javax.inject.Inject

class DemandsFragment: BaseFragment(), OffersAdapter.IListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: DemandsViewModel
    private lateinit var binding: DemandsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity().application as AppApplication).getComponent().inject(this)
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

    override fun onPositiveClick() {
        super.onPositiveClick()
        viewModel.removeShownError()
    }

    override fun onItemClick(item: Service) {
        Log.e(App.TAG, "On item click ${item.title}")
        Toast.makeText(
            requireContext(),
            "Show contacts is not implemented yet for service '${item.title}'",
            Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_demandsFragment_to_contactsFragment)
    }

    private fun setupList() {
        binding.demandsList.adapter = OffersAdapter(this)
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    Log.d(App.PAGING, "[offers] get update from viewmodel ${it.pagingData}")
                    binding.progressIndicator.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                    if (it.errors.isNotEmpty()) {
                        showErrorDialog(it.errors.get(0))
                    }
                    if (it.pagingData != null) {
                        (binding.demandsList.adapter as OffersAdapter).submitData(it.pagingData)
                    }
                }
            }
        }
        lifecycleScope.launch {
            (binding.demandsList.adapter as OffersAdapter).loadStateFlow.collectLatest { loadState ->
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