package com.college.culinaryexchange.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.college.culinaryexchange.databinding.FragmentHomeBinding
import com.college.culinaryexchange.ui.post.PostAdapter
import com.college.culinaryexchange.util.SeedDataUtil
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val quoteAdapter = QuoteAdapter(
            quote = viewModel.currentQuote.value ?: Quote("", ""),
            onRefresh = { viewModel.refreshQuote() }
        )
        val postAdapter = PostAdapter(
            onItemClick = { post ->
                val action = HomeFragmentDirections.actionHomeToRecipeDetail(postId = post.id)
                findNavController().navigate(action)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ConcatAdapter(quoteAdapter, postAdapter)

        viewModel.currentQuote.observe(viewLifecycleOwner) { quote ->
            quoteAdapter.updateQuote(quote)
        }

        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        lifecycleScope.launch {
            runCatching { SeedDataUtil.seedIfEmpty() }
                .onFailure { Log.e("HomeFragment", "Seed failed: ${it.message}", it) }
            viewModel.loadPosts()
        }

        // Drawer open/close
        binding.btnOpenFilter.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
        binding.btnCloseFilter.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // Sort order
        binding.radioGroupSort.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioNewest.id -> viewModel.setSortOrder(SortOrder.NEWEST_FIRST)
                binding.radioOldest.id -> viewModel.setSortOrder(SortOrder.OLDEST_FIRST)
            }
        }

        // Category chips
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedId)
            val category = chip?.text?.toString() ?: "All"
            viewModel.setCategory(category)
        }

        // Reset filters
        binding.btnResetFilters.setOnClickListener {
            binding.radioGroupSort.check(binding.radioNewest.id)
            binding.chipGroupCategory.check(binding.chipAll.id)
            viewModel.resetFilters()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
