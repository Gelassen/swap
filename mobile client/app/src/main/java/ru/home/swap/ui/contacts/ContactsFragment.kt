package ru.home.swap.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.home.swap.databinding.ContactsFragmentBinding
import ru.home.swap.ui.common.BaseFragment

class ContactsFragment : BaseFragment() {

    private lateinit var binding: ContactsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContactsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}