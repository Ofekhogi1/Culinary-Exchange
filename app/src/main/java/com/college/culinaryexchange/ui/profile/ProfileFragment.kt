package com.college.culinaryexchange.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.college.culinaryexchange.R
import com.college.culinaryexchange.databinding.FragmentProfileBinding
import com.college.culinaryexchange.ui.post.PostAdapter

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postAdapter = PostAdapter(
            showActions = true,
            onEdit = { post ->
                val action = ProfileFragmentDirections.actionProfileToAddPost(post.id)
                findNavController().navigate(action)
            },
            onDelete = { post -> viewModel.deletePost(post.id) }
        )
        binding.rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserPosts.adapter = postAdapter

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.tvName.text = user.name
            binding.tvEmail.text = user.email
            if (user.avatarUrl.isNotBlank()) {
                Glide.with(this)
                    .load(user.avatarUrl)
                    .transform(CircleCrop())
                    .into(binding.ivAvatar)
            }
        }

        viewModel.recipeCount.observe(viewLifecycleOwner) { count ->
            binding.tvRecipeCount.text = count.toString()
        }

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        binding.btnEditAvatar.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_editProfile)
        }

        viewModel.loadUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
