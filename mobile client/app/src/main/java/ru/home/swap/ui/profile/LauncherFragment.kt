package ru.home.swap.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.databinding.LauncherFragmentBinding
import ru.home.swap.di.ViewModelFactory
import javax.inject.Inject

class LauncherFragment: Fragment() {

    private lateinit var binding: LauncherFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as AppApplication).getComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        binding = LauncherFragmentBinding.inflate(inflater, container, false)
        binding.state = viewModel.uiState.value
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    when(it.status) {
                        StateFlag.CREDENTIALS -> {
                            findNavController().navigate(R.id.action_launcherFragment_to_signInFragment)
                        }
                        StateFlag.PROFILE -> {
                            Log.d(App.TAG, "[navigation] call navigation action from launcher fragment")
                            findNavController().navigate(R.id.action_launcherFragment_to_profileFragment)
                        }
                        StateFlag.NONE -> {
                            if (it.errors.isNotEmpty()) {
                                val error = it.errors.first()
                                Toast.makeText(requireContext(),
                                    "There is an error: $error",
                                    Toast.LENGTH_SHORT)
                                    .show()
                                viewModel.removeShownError()
                            }

                        }
                    }
                }
            }
        }
        viewModel.checkAnExistingAccount()
    }
}