package ru.home.swap.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_item_fragment.*
import ru.home.swap.App
import ru.home.swap.AppApplication
import ru.home.swap.R
import ru.home.swap.databinding.AddItemFragmentBinding
import ru.home.swap.di.ViewModelFactory
import javax.inject.Inject


class AddItemBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private lateinit var binding: AddItemFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        (requireActivity().application as AppApplication).getComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        binding = AddItemFragmentBinding.inflate(inflater, container, false)
        binding.model = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.offer_option)
            .setOnClickListener {
                group_choice.onOfferClick()
            }

        view.findViewById<TextView>(R.id.demand_option)
            .setOnClickListener {
                group_choice.onDemandClick()
            }

        save.setOnClickListener {
            Log.d(App.TAG, "${viewModel.proposal.get()}")
            viewModel.addItem()
            dismiss()
        }
    }

    companion object {

        const val TAG = "AddItemBottomSheetDialogFragment"

        fun newInstance(args: Bundle): AddItemBottomSheetDialogFragment{
            val fragment = AddItemBottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}