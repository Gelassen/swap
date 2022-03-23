package ru.home.swap.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import ru.home.swap.R

@Deprecated(message = "Legacy code. Should be removed.")
class LoginDialogFragment(): DialogFragment(R.layout.signin_fragment) {

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Credentials")
//            .setMessage("Please enter your credentials")
//            .create()
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}

