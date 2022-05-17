package ru.home.swap.ui.chains

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.home.swap.databinding.ChainsFragmentBinding
import ru.home.swap.ui.common.BaseFragment

class ChainsFragment: BaseFragment() {

    private lateinit var binding: ChainsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChainsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}