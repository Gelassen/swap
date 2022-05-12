package ru.home.swap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.home.swap.databinding.TestFragmentBinding
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.ui.profile.ProfileViewModel
import javax.inject.Inject

class TestFragment: Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    lateinit var viewModel: ProfileViewModel

    lateinit var binding: TestFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity().application as AppApplication).getComponent().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        binding = TestFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*        click_me.setOnClickListener {
//            viewModel.add(System.currentTimeMillis())
            viewModel.proposal.set("New item ${System.currentTimeMillis()}")
            viewModel.addOffer()
        }*/

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
//                viewModel.testState.collect { it ->
//                    Log.d(App.TAG, "Collect is called for new value $it")
//                }
                viewModel.uiState.collect { it ->
                    Log.d(App.TAG, "Collect is called for a new value ${it.offers.count()}")
                }
            }
        }

    }
}