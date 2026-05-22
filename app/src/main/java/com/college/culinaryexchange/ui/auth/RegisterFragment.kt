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
import com.college.culinaryexchange.databinding.FragmentRegisterBinding
import com.college.culinaryexchange.util.Validators

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            when {
                !Validators.isValidName(name) -> {
                    Toast.makeText(requireContext(), "Name must be at least 2 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !Validators.isValidEmail(email) -> {
                    Toast.makeText(requireContext(), "Enter a valid email address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !Validators.isStrongPassword(password) -> {
                    Toast.makeText(
                        requireContext(),
                        "Password must be 8+ characters with a capital letter and a number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            viewModel.register(name, email, password)
        }

        binding.btnGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state is AuthState.Loading) View.VISIBLE else View.GONE
            when (state) {
                is AuthState.Success -> findNavController().navigate(R.id.action_register_to_home)
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
