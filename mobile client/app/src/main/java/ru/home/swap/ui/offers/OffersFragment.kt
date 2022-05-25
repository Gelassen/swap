package ru.home.swap.ui.offers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.databinding.OffersFragmentBinding
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.model.Service
import ru.home.swap.ui.common.BaseFragment
import javax.inject.Inject


class OffersFragment: BaseFragment(), OffersAdapter.IListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: OffersViewModel
    private lateinit var binding: OffersFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity().application as AppApplication).getComponent().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(OffersViewModel::class.java)

        binding = OffersFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        setupList()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchOffers()
        Log.d(App.TAG, "[offers] onViewCreated()")
    }

    private fun fetchOffers() {
        lifecycleScope.launch {
            lifecycleScope.launch {
                viewModel
                    .getOffers()
                    .collectLatest { it ->
                        Log.d(App.TAG, "[offers] collectLatest()")
                        (binding.offersList.adapter as OffersAdapter).submitData(it)
                    }
            }
            lifecycleScope.launch {
                (binding.offersList.adapter as OffersAdapter).loadStateFlow.collectLatest { loadState ->
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            // no op
                        }
                        is LoadState.Error -> {
                            showErrorDialog((loadState.refresh as LoadState.Error).error.localizedMessage)
//                            showError((loadState.refresh as LoadState.Error).error.localizedMessage)
                        }
                        is LoadState.NotLoading -> {
//                            visibleProgress(false)
                            val isNoContent = binding.offersList.adapter!!.itemCount == 0
                            binding.noContent.visibility = if (isNoContent) View.VISIBLE else View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(item: Service) {
        Log.e(App.TAG, "On item click ${item.title}")
        Toast.makeText(
            requireContext(),
            "Show contacts is not implemented yet for service '${item.title}'",
            Toast.LENGTH_SHORT).show()
    }

    private fun setupList() {
        binding.offersList.adapter = OffersAdapter(this)
        binding.offersList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.offersList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                (binding.offersList.layoutManager as LinearLayoutManager).getOrientation()
            )
        )
    }
}