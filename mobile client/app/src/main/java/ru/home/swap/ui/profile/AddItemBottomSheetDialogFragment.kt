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
import dagger.android.support.AndroidSupportInjection
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.databinding.AddItemFragmentBinding
import ru.home.swap.core.di.ViewModelFactory
import java.lang.IllegalStateException
import javax.inject.Inject


class AddItemBottomSheetDialogFragment: BottomSheetDialogFragment() {

    interface IAddItemDialog {
        fun onAddItemDialogDismiss()
    }

    companion object {

        const val TAG = "AddItemBottomSheetDialogFragment"

        fun newInstance(args: Bundle): AddItemBottomSheetDialogFragment{
            val fragment = AddItemBottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: AddItemFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: ProfileV2ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // keep an eye on owner parameter, it should be the same scope for view model which is shared among component
        viewModel = ViewModelProvider(requireParentFragment(), viewModelFactory).get(ProfileV2ViewModel::class.java)
        binding = AddItemFragmentBinding.inflate(inflater, container, false)
        binding.model = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentFragment !is IAddItemDialog) {
            throw IllegalStateException("Did you forget to implement IAddItemDialog interface? " +
                    "One of the parent's fragments in the hierarchy must respond on addItem() action.")
        }

        view.findViewById<TextView>(R.id.offer_option)
            .setOnClickListener {
                binding.groupChoice.onOfferClick()
            }

        view.findViewById<TextView>(R.id.demand_option)
            .setOnClickListener {
                binding.groupChoice.onDemandClick()
            }

        binding.save.setOnClickListener {
            Log.d(App.TAG, "${viewModel.proposal.get()}")
            viewModel.addItem(binding.groupChoice.isOfferSelected())
            (parentFragment as IAddItemDialog).onAddItemDialogDismiss()
            dismiss()
        }
    }
}