package com.college.culinaryexchange.ui.post

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.college.culinaryexchange.databinding.FragmentMyPostsBinding

class MyPostsFragment : Fragment() {

    private var _binding: FragmentMyPostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PostAdapter(
            showActions = true,
            onEdit = { post ->
                val action = MyPostsFragmentDirections.actionMyPostsToAddPost(postId = post.id)
                findNavController().navigate(action)
            },
            onDelete = { post -> viewModel.deletePost(post.id) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadUserPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
