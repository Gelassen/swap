package ru.home.swap.ui.common

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.home.swap.R

class ErrorDialogFragment: DialogFragment() {

    companion object {

        const val TAG: String = "ErrorDialogFragment"
        const val KEY_TITLE = "KEY_TITLE"
        const val KEY_MESSAGE = "KEY_MESSAGE"

        fun newInstance(resources: Resources, message: String): ErrorDialogFragment {
            return newInstance(resources.getString(R.string.default_error_title_dialog), message)
        }

        fun newInstance(title: String, message: String): ErrorDialogFragment {
            val errorDialogFragment = ErrorDialogFragment()
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_MESSAGE, message)
            errorDialogFragment.arguments = args
            return errorDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(requireArguments().getString(KEY_TITLE))
            .setMessage(requireArguments().getString(KEY_MESSAGE))
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i -> /* no op */ })
            .create()
    }

}