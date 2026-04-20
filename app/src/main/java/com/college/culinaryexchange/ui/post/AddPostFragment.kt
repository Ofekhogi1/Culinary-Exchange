package com.college.culinaryexchange.ui.post

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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.databinding.FragmentAddPostBinding
import com.college.culinaryexchange.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private val args: AddPostFragmentArgs by navArgs()

    private var selectedImageUri: Uri? = null
    private var existingPost: PostEntity? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImage.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId
        val isEditMode = postId != null

        binding.btnSubmit.text = if (isEditMode) "Update" else "Post"

        if (isEditMode) {
            viewModel.loadPostForEdit(postId!!)
            viewModel.editPost.observe(viewLifecycleOwner) { post ->
                post ?: return@observe
                existingPost = post
                binding.etTitle.setText(post.title)
                binding.etDescription.setText(post.description)
                if (post.imageUrl.isNotBlank()) {
                    Glide.with(this).load(post.imageUrl).into(binding.ivPostImage)
                }
            }
        }

        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            if (title.isBlank()) {
                Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                val existing = existingPost ?: return@setOnClickListener
                val updatedPost = Post(
                    id = existing.id,
                    userId = existing.userId,
                    userName = existing.userName,
                    userAvatarUrl = existing.userAvatarUrl,
                    title = title,
                    description = description,
                    imageUrl = existing.imageUrl,
                    timestamp = existing.timestamp
                )
                viewModel.updatePost(updatedPost, selectedImageUri)
            } else {
                resolveUserName { userName ->
                    viewModel.createPost(title, description, userName, selectedImageUri)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !loading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { findNavController().popBackStack() }
                .onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun resolveUserName(onResolved: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onResolved("Anonymous")
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Anonymous"
                onResolved(name)
            }
            .addOnFailureListener { onResolved("Anonymous") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
