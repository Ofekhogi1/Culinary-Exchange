package com.college.culinaryexchange.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.college.culinaryexchange.R
import com.college.culinaryexchange.databinding.FragmentLoginBinding
import com.college.culinaryexchange.util.Validators

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            when {
                !Validators.isValidEmail(email) -> {
                    Toast.makeText(requireContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !Validators.isValidPassword(password) -> {
                    Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            viewModel.login(email, password)
        }

        binding.btnGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state is AuthState.Loading) View.VISIBLE else View.GONE
            when (state) {
                is AuthState.Success -> findNavController().navigate(R.id.action_login_to_home)
                is AuthState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
