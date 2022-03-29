package ru.home.swap.ui.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.profile_fragment.*
import ru.home.swap.App
import ru.home.swap.databinding.ProfileFragmentBinding


class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var binding: ProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.apply {
            setOnClickListener {
                childFragmentManager.let {
                    AddItemBottomSheetDialogFragment.newInstance(Bundle.EMPTY)
                        .show(it, AddItemBottomSheetDialogFragment.TAG)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(App.TAG, "Intercept on back press in profile fragment")
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    fun onFabClick(view: View) {
        childFragmentManager.let {
            AddItemBottomSheetDialogFragment.newInstance(Bundle.EMPTY).show(it, tag)
        }
    }
}