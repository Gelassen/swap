package ru.home.swap.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.databinding.ContactsFragmentBinding
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.common.IDialogListener
import ru.home.swap.ui.contacts.ContactsFragment.Params.EXTRA_SERVICE_ID
import javax.inject.Inject

class ContactsFragment : BaseFragment() {

    object Params {
        const val EXTRA_SERVICE_ID = "serviceId"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ContactsViewModel

    private lateinit var binding: ContactsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity().application as AppApplication).getComponent().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ContactsViewModel::class.java)
        binding = ContactsFragmentBinding.inflate(inflater, container, false)
        binding.name = viewModel.uiState.value.counterpartyProfile?.name
        binding.contact = viewModel.uiState.value.counterpartyProfile?.contact
        Log.d(App.TAG, "Service id ${requireArguments().get(EXTRA_SERVICE_ID)}")
        hideBottomNavigationView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    Log.d(App.TAG, "[contacts] collect call ${it.counterpartyProfile}")
                    if (it.errors.isNotEmpty()) {
                        showErrorDialog(it.errors.first())
                    }
                    binding.name = it.counterpartyProfile?.name
                    binding.contact = it.counterpartyProfile?.contact
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.fetchContacts(requireArguments().getLong(EXTRA_SERVICE_ID))
            }
        }
    }

    override fun onPositiveClick() {
        super.onPositiveClick()
        viewModel.removeShownError()
    }
}