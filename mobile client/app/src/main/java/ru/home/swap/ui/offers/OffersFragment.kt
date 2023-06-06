package ru.home.swap.ui.offers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.core.model.Service
import ru.home.swap.databinding.OffersFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.contacts.ContactsFragment
import javax.inject.Inject


class OffersFragment: BaseFragment(), OffersAdapter.IListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: OffersViewModel
    private lateinit var binding: OffersFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(OffersViewModel::class.java)

        binding = OffersFragmentBinding.inflate(inflater, container, false)
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
        val bundle = bundleOf(ContactsFragment.Params.EXTRA_SERVICE_ID to item.id) // FIXME just a stub due refactoring, reimplemenet it
        findNavController().navigate(R.id.action_offersFragment_to_contactsFragment, bundle)
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

    private fun listenUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.uiState.collect { it ->
                        Log.d(App.PAGING, "[offers] get update from viewmodel ${it.pagingData}")
                        binding.progressIndicator.visibility = if (it.isLoading) View.VISIBLE else View.GONE
                        if (it.errors.isNotEmpty()) {
                            showErrorDialog(it.errors.get(0))
                        }
                        if (it.pagingData != null) {
//                            throw UnsupportedOperationException("Not supported yet for new type ${it.pagingData}")
                            (binding.offersList.adapter as OffersAdapter).submitData(it.pagingData)
                        }
                        binding.noContent.visibility = if (binding.offersList.adapter!!.itemCount == 0) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    (binding.offersList.adapter as OffersAdapter).loadStateFlow.collectLatest { loadState ->
                        when (loadState.refresh) {
                            is LoadState.Loading -> {
                                // no op
                            }
                            is LoadState.Error -> {
                                viewModel.addError((loadState.refresh as LoadState.Error).error.localizedMessage!!)
                            }
                            is LoadState.NotLoading -> {
                                val isNoContent = binding.offersList.adapter!!.itemCount == 0
                                binding.noContent.visibility =
                                    if (isNoContent) View.VISIBLE else View.GONE
                                binding.progressIndicator.visibility = View.GONE
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