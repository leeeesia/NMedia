package ru.netology.nmedia.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private val viewModel: SignUpViewModel by viewModels()
    @Inject
    lateinit var appAuth: AppAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        binding.registration.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            val passwordRep = binding.password2.text.toString()
            if (login.isBlank() || password.isBlank() || name.isBlank() || passwordRep.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_empty_content),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
             if (password != passwordRep) {
                 Snackbar.make(
                     binding.root,
                     getString(R.string.passwords_not_match),
                     Snackbar.LENGTH_SHORT
                 ).show()
                 return@setOnClickListener
             }

            viewModel.signUp(login, password, name)

            lifecycleScope.launchWhenCreated {
                appAuth.state.collectLatest {
                    it?.let { findNavController().navigateUp() }
                }
            }
        }

        return binding.root
    }
}
