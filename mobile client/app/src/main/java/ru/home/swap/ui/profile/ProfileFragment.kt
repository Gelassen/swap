package ru.home.swap.ui.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.core.model.Service
import ru.home.swap.databinding.ProfileFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.ui.common.BaseFragment
import javax.inject.Inject


class ProfileFragment : BaseFragment(), ItemAdapter.Listener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileV2ViewModel

    private lateinit var binding: ProfileFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // keep an eye on owner parameter, it should be the same scope for view model which is shared among components
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileV2ViewModel::class.java)
        binding = ProfileFragmentBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showBottomNavigationView()
        binding.offersList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.offersList.adapter = ItemAdapter(true, this)

        binding.demandsList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.demandsList.adapter = ItemAdapter(false, this)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Log.d(App.TAG, "[collect] UI state collect is called")
                viewModel.uiState.collect { it ->
                    Log.d(App.TAG, "[collect] collected #${it.profile.offers.count()} offers items")
                    (binding.offersList.adapter as ItemAdapter).submitList(it.profile.offers)
                    Log.d(App.TAG, "[collect] collected #${it.profile.demands.count()} demands items")
                    (binding.demandsList.adapter as ItemAdapter).submitList(it.profile.demands)
                    onModelUpdate(it)
                }
            }
        }
        binding.fab.apply {
            setOnClickListener {
                childFragmentManager.let {
                    Log.d(App.TAG, "[fab click] Offers count: ${viewModel.uiState.value.profile.offers.count()}")
                    AddItemBottomSheetDialogFragment.newInstance(Bundle.EMPTY)
                        .show(it, AddItemBottomSheetDialogFragment.TAG)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(App.TAG, "Intercept on back press in profile fragment")
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onRemove(item: Service, isOffers: Boolean) {
        if (isOffers) {
            viewModel.removeOffer(item)
        } else {
            viewModel.removeDemand(item)
        }
    }

    override fun onPositiveClick() {
        viewModel.removeShownError()
    }
}