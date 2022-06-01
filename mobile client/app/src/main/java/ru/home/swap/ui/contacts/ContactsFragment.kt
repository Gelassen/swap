package ru.home.swap.ui.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.home.swap.App
import ru.home.swap.databinding.ContactsFragmentBinding
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.contacts.ContactsFragment.Params.EXTRA_SERVICE_ID

class ContactsFragment : BaseFragment() {

    object Params {
        const val EXTRA_SERVICE_ID = "serviceId"
    }

    private lateinit var binding: ContactsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContactsFragmentBinding.inflate(inflater, container, false)
        Log.d(App.TAG, "Service id ${requireArguments().get(EXTRA_SERVICE_ID)}")
        return binding.root
    }
}