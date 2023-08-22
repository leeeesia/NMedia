package ru.netology.nmedia.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSigninBinding
import ru.netology.nmedia.viewmodel.SignInViewModel


class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentSigninBinding.inflate(inflater, container, false)

        binding.signIn.setOnClickListener {
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            if (login.isBlank() || password.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_empty_content),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.signIn(login, password)

            findNavController().navigateUp()
        }

        return binding.root
    }
}
