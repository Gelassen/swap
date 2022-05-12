package ru.home.swap.ui.common

import androidx.fragment.app.Fragment
import ru.home.swap.R
import ru.home.swap.ui.profile.Model

abstract class BaseFragment: Fragment(), IDialogListener {

    override fun onPositiveClick() {
        // no op, optionally override in child Fragment
    }

    fun showErrorDialog(str: String) {
        ErrorDialogFragment.newInstance(
            getString(R.string.default_error_title_dialog),
            str
        ).show(childFragmentManager, ErrorDialogFragment.TAG)
    }

    fun onModelUpdate(model: Model) {
        if (model.errors.isNotEmpty()) {
            showErrorDialog(model.errors.first())
        }
    }
}