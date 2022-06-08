package ru.home.swap.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.databinding.ContactsFragmentBinding
import ru.home.swap.di.AppModule
import ru.home.swap.di.NetworkModule
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.repository.Cache
import ru.home.swap.repository.PersonRepository
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.common.IDialogListener
import ru.home.swap.ui.contacts.ContactsFragment.Params.EXTRA_SERVICE_ID
import java.lang.RuntimeException
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
        binding.lifecycleOwner = this
        binding.name = viewModel.uiState.value.counterpartyProfile?.name
        binding.contact = viewModel.uiState.value.counterpartyProfile?.contact
        hideBottomNavigationView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.uiState.collect(FlowCollector { it->
                if (it.errors.isNotEmpty()) {
                    showErrorDialog(it.errors.first())
                }
                binding.name = it.counterpartyProfile?.name
                binding.contact = it.counterpartyProfile?.contact
            })
        }
        lifecycleScope.launch {
            Log.d(App.TAG, "Extra service id ${requireArguments().getLong(EXTRA_SERVICE_ID)}")
            viewModel.fetchContacts(requireArguments().getLong(EXTRA_SERVICE_ID))
        }
    }

    override fun onPositiveClick() {
        super.onPositiveClick()
        viewModel.removeShownError()
    }
}