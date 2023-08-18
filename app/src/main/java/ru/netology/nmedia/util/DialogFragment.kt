package ru.netology.nmedia.util

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth

open class MyDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            when (tag) {
                "signin" -> builder.setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.need_login))
                    .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                        findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                        dialog.cancel()
                    }
                "logout" -> builder.setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.get_out))
                    .setPositiveButton(getString(R.string.yes)) { dialog, id ->
                        AppAuth.getInstance().clearAuth()
                        findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                        dialog.cancel()
                    }
                    .setNegativeButton(getString(R.string.cansel)) { dialog, id ->
                        dialog.cancel()
                    }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}