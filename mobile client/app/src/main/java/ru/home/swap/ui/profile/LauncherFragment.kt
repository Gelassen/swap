package ru.home.swap.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.databinding.LauncherFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.common.ErrorDialogFragment
import javax.inject.Inject

class LauncherFragment: BaseFragment() {

    private lateinit var binding: LauncherFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
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
        hideBottomNavigationView()

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
                            /* no op */
                        }
                    }
                    if (it.errors.isNotEmpty()) {
                        Log.d(App.TAG, "Error: " + it.errors.first())
                        ErrorDialogFragment.newInstance(getString(R.string.default_error_title_dialog), it.errors.first())
                            .show(childFragmentManager, ErrorDialogFragment.TAG)
                    }
                }
            }
        }
        viewModel.checkAnExistingAccount()
    }

    override fun onPositiveClick() {
        viewModel.removeShownError()
        requireActivity().finish()
    }

}