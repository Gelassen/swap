package ru.home.swap.ui.common

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.home.swap.App
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

    fun showBottomNavigationView() {
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar)
        if (navView != null) {
            navView.visibility = View.VISIBLE
        } else {
            Log.e(App.TAG, "Navigation view should not be null here")
        }
    }

    fun hideBottomNavigationView() {
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar)
        if (navView != null) {
            navView.visibility = View.GONE
        } else {
            Log.e(App.TAG, "Navigation view should not be null here")
        }
    }
}