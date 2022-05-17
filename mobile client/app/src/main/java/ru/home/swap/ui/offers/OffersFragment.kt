package ru.home.swap.ui.offers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.home.swap.databinding.OffersFragmentBinding
import ru.home.swap.ui.common.BaseFragment

class OffersFragment: BaseFragment() {

    private lateinit var binding: OffersFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OffersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}