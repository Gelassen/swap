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
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.home.swap.App
import ru.home.swap.BuildConfig
import ru.home.swap.R
import ru.home.swap.core.model.DebugProfiles
import ru.home.swap.databinding.SigninFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import ru.home.swap.providers.PersonProvider
import ru.home.swap.ui.common.BaseFragment
import ru.home.swap.wallet.TestWalletActivity
import ru.home.swap.wallet.contract.Value
import java.math.BigInteger
import javax.inject.Inject

class SignInFragment: BaseFragment() {

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var binding: SigninFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileV2ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SigninFragmentBinding.inflate(inflater, container, false)
        binding.provider = PersonProvider()
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileV2ViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar!!.hide()
        hideBottomNavigationView()

        binding.model = viewModel/*viewModel.uiState.value*/
        binding.lifecycleOwner = this
        binding.confirm.setOnClickListener {
            viewModel.createAnAccount()
        }
        binding.progressIndicator.visibility = View.GONE

        if (BuildConfig.DEBUG) {
            val profiles = DebugProfiles()
            binding.debugBadge.visibility = View.VISIBLE
            binding.debugBadge.setOnClickListener {
                val profile = profiles.next()
                binding.name.setText(profile.name)
                binding.contactPhone.setText(profile.contact)
                binding.secret.setText(profile.secret)
                binding.walletAddress.setText(profile.userWalletAddress)
            }

            binding.debugClickMe.visibility = View.VISIBLE
            binding.debugClickMe.setOnClickListener {
                val to = TestWalletActivity.SECOND_USER/*getString(R.string.my_account)*/
                val value = Value(
                    TestWalletActivity.SECOND_USER_OFFER,
                    BigInteger.valueOf(1665158348220),
                    BigInteger.valueOf(1669758348220),
                    false,
                    BigInteger.valueOf(0)
                )
                val uri = "https://gelassen.github.io/blog/"
                viewModel.mintToken(to, value, uri)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { it ->
                    when(it.status) {
                        StateFlagV2.CREDENTIALS -> {
                            Log.d(App.TAG, "[6a] UI state: credentials")
                        }
                        StateFlagV2.PROFILE -> {
                            Log.d(App.TAG, "[6b] UI state: profile")
                            findNavController().navigate(R.id.action_signInFragment_to_profileFragment)
                        }
                        StateFlagV2.NONE -> {
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