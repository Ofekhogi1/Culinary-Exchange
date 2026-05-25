package com.college.culinaryexchange.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.college.culinaryexchange.databinding.FragmentEditProfileBinding
import com.college.culinaryexchange.util.ImageLoader

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()

    private var selectedAvatarUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedAvatarUri = it
            Glide.with(this).load(it).circleCrop().into(binding.ivAvatar)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUser()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.ivCameraBtn.setOnClickListener { pickImage.launch("image/*") }
        binding.ivAvatar.setOnClickListener { pickImage.launch("image/*") }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.etName.setText(user.name)
            binding.etEmail.setText(user.email)
            binding.etBio.setText(user.bio)
            if (user.avatarUrl.isNotBlank()) {
                ImageLoader.loadCircle(requireContext(), user.avatarUrl, binding.ivAvatar)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !loading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            result.onSuccess {
                viewModel.clearOperationResult()
                findNavController().popBackStack()
            }.onFailure {
                viewModel.clearOperationResult()
                Toast.makeText(requireContext(), it.message ?: "Error saving profile", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveUser(name, bio, selectedAvatarUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
