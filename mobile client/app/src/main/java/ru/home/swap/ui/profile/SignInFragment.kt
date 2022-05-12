package ru.home.swap.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.home.swap.databinding.SigninFragmentBinding
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.providers.PersonProvider
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.ui.common.ErrorDialogFragment
import ru.home.swap.ui.common.IDialogListener
import javax.inject.Inject

class SignInFragment: BaseFragment() {

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var binding: SigninFragmentBinding

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
        binding = SigninFragmentBinding.inflate(inflater, container, false)
        binding.provider = PersonProvider()
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()

        binding.model = viewModel/*viewModel.uiState.value*/
        binding.lifecycleOwner = this
        binding.confirm.setOnClickListener {
            viewModel.createAnAccount()
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    when(it.status) {
                        StateFlag.CREDENTIALS -> {
                            Log.d(App.TAG, "[6a] UI state: credentials")
                        }
                        StateFlag.PROFILE -> {
                            Log.d(App.TAG, "[6b] UI state: profile")
                            findNavController().navigate(R.id.action_signInFragment_to_profileFragment)
                        }
                        StateFlag.NONE -> {
                            Log.d(App.TAG, "[6c] UI state: none ")
                        }
                    }
                    onModelUpdate(it)
                }
            }
        }
    }

    override fun onPositiveClick() {
        viewModel.removeShownError()
    }
}