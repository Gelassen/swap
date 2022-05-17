package ru.home.swap.ui.demands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.home.swap.databinding.DemandsFragmentBinding
import ru.home.swap.ui.common.BaseFragment

class DemandsFragment: BaseFragment() {

    private lateinit var binding: DemandsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DemandsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}