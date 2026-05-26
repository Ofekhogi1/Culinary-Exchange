package com.college.culinaryexchange.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.college.culinaryexchange.R
import com.college.culinaryexchange.util.ImageLoader
import com.college.culinaryexchange.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeDetailViewModel by viewModels()
    private val args: RecipeDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        viewModel.loadPost(args.postId)

        viewModel.notFound.observe(viewLifecycleOwner) { missing ->
            if (missing) {
                android.widget.Toast.makeText(
                    requireContext(), "Recipe not found", android.widget.Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }

        viewModel.post.observe(viewLifecycleOwner) { post ->
            post ?: return@observe

            binding.tvTitle.text = post.title
            binding.tvOwner.text = "By ${post.userName}"
            binding.tvDescription.text = post.description

            binding.tvPrepTime.text = if (post.prepTime > 0) "⏱ ${post.prepTime} min" else "⏱ —"
            binding.tvServings.text = if (post.servings > 0) "👥 ${post.servings} servings" else "👥 —"

            binding.tvCalories.text = if (post.nutritionCalories > 0) "${post.nutritionCalories}" else "—"
            binding.tvProtein.text = if (post.nutritionProtein.isNotBlank()) post.nutritionProtein else "—"
            binding.tvCarbs.text = if (post.nutritionCarbs.isNotBlank()) post.nutritionCarbs else "—"
            binding.tvFat.text = if (post.nutritionFat.isNotBlank()) post.nutritionFat else "—"

            if (post.imageUrl.isNotBlank()) {
                ImageLoader.load(requireContext(), post.imageUrl, binding.ivHero)
            }

            binding.layoutIngredients.removeAllViews()
            post.ingredients.forEach { ingredient ->
                val density = resources.displayMetrics.density
                val tv = TextView(requireContext()).apply {
                    text = "• $ingredient"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    val p = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    p.bottomMargin = (6 * density).toInt()
                    layoutParams = p
                }
                binding.layoutIngredients.addView(tv)
            }

            binding.layoutInstructions.removeAllViews()
            post.instructions.forEachIndexed { index, step ->
                addInstructionStep(index + 1, step)
            }
        }
    }

    private fun addInstructionStep(number: Int, text: String) {
        val density = resources.displayMetrics.density
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.TOP
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            p.bottomMargin = (12 * density).toInt()
            layoutParams = p
        }

        val badgeSize = (28 * density).toInt()
        val badge = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize).also {
                it.marginEnd = (12 * density).toInt()
                it.topMargin = (2 * density).toInt()
            }
            background = resources.getDrawable(R.drawable.bg_step_badge, null)
        }
        val badgeText = TextView(requireContext()).apply {
            this.text = number.toString()
            textSize = 12f
            setTextColor(resources.getColor(android.R.color.white, null))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.CENTER
            )
        }
        badge.addView(badgeText)

        val stepText = TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_primary, null))
            setLineSpacing(0f, 1.4f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        row.addView(badge)
        row.addView(stepText)
        binding.layoutInstructions.addView(row)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
