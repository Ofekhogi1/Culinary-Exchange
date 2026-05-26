package com.college.culinaryexchange.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.college.culinaryexchange.R
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.databinding.FragmentAddPostBinding
import com.college.culinaryexchange.model.Post
import com.college.culinaryexchange.util.ImageLoader
import com.google.android.material.chip.Chip

class AddPostFragment : Fragment() {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private val args: AddPostFragmentArgs by navArgs()

    private var selectedImageUri: Uri? = null
    private var existingPost: PostEntity? = null

    private val ingredients = mutableListOf<String>()
    private val instructions = mutableListOf<String>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).centerCrop().into(binding.ivPostImage)
            ImageViewCompat.setImageTintList(binding.ivPostImage, null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId
        val isEditMode = postId != null

        binding.tvTitle.text = if (isEditMode) "Edit Recipe" else "New Recipe"
        binding.btnSubmit.text = if (isEditMode) "Update Recipe" else "Create Recipe"

        if (isEditMode) {
            viewModel.loadPostForEdit(postId!!)
            viewModel.editPost.observe(viewLifecycleOwner) { post ->
                post ?: return@observe
                existingPost = post
                populateFormForEdit(post)
            }
        }

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }

        binding.btnAddIngredient.setOnClickListener {
            val text = binding.etIngredient.text.toString().trim()
            if (text.isNotEmpty()) {
                ingredients.add(text)
                addIngredientChip(text)
                binding.etIngredient.text?.clear()
            }
        }

        binding.btnAddInstruction.setOnClickListener {
            val text = binding.etInstruction.text.toString().trim()
            if (text.isNotEmpty()) {
                instructions.add(text)
                addInstructionStep(instructions.size, text)
                binding.etInstruction.text?.clear()
            }
        }

        binding.btnSubmit.setOnClickListener { onSubmit(isEditMode) }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !loading
        }

        viewModel.operationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { findNavController().popBackStack() }
                .onFailure { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun populateFormForEdit(post: PostEntity) {
        binding.etTitle.setText(post.title)
        binding.etDescription.setText(post.description)
        binding.etPrepTime.setText(if (post.prepTime > 0) post.prepTime.toString() else "")
        binding.etServings.setText(if (post.servings > 0) post.servings.toString() else "")

        if (post.imageUrl.isNotBlank()) {
            ImageLoader.load(requireContext(), post.imageUrl, binding.ivPostImage)
            ImageViewCompat.setImageTintList(binding.ivPostImage, null)
        }
        if (post.category.isNotBlank()) {
            for (i in 0 until binding.chipGroupCategory.childCount) {
                val chip = binding.chipGroupCategory.getChildAt(i) as? Chip
                if (chip?.text.toString() == post.category) { chip?.isChecked = true; break }
            }
        }
        if (post.nutritionCalories > 0) binding.etCalories.setText(post.nutritionCalories.toString())
        if (post.nutritionProtein.isNotBlank()) binding.etProtein.setText(post.nutritionProtein)
        if (post.nutritionCarbs.isNotBlank()) binding.etCarbs.setText(post.nutritionCarbs)
        if (post.nutritionFat.isNotBlank()) binding.etFat.setText(post.nutritionFat)

        ingredients.clear(); ingredients.addAll(post.ingredients)
        post.ingredients.forEach { addIngredientChip(it) }

        instructions.clear(); instructions.addAll(post.instructions)
        post.instructions.forEachIndexed { index, text -> addInstructionStep(index + 1, text) }
    }

    private fun onSubmit(isEditMode: Boolean) {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val prepTime = binding.etPrepTime.text.toString().trim().toIntOrNull() ?: 0
        val servings = binding.etServings.text.toString().trim().toIntOrNull() ?: 0
        val selectedCategoryId = binding.chipGroupCategory.checkedChipId
        val category = if (selectedCategoryId != -1)
            binding.chipGroupCategory.findViewById<Chip>(selectedCategoryId)?.text?.toString() ?: ""
        else ""
        val calories = binding.etCalories.text.toString().trim().toIntOrNull() ?: 0
        val protein = binding.etProtein.text.toString().trim()
        val carbs = binding.etCarbs.text.toString().trim()
        val fat = binding.etFat.text.toString().trim()

        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (ingredients.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one ingredient", Toast.LENGTH_SHORT).show()
            return
        }
        if (instructions.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one instruction step", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode) {
            val existing = existingPost ?: return
            viewModel.updatePost(
                Post(
                    id = existing.id,
                    userId = existing.userId,
                    userName = existing.userName,
                    userAvatarUrl = existing.userAvatarUrl,
                    title = title,
                    description = description,
                    imageUrl = existing.imageUrl,
                    timestamp = existing.timestamp,
                    ingredients = ingredients.toList(),
                    instructions = instructions.toList(),
                    prepTime = prepTime,
                    servings = servings,
                    category = category,
                    nutritionCalories = calories,
                    nutritionProtein = protein,
                    nutritionCarbs = carbs,
                    nutritionFat = fat
                ),
                selectedImageUri
            )
        } else {
            // Username is resolved inside PostViewModel via AuthRepository — no Firestore call here
            viewModel.createPost(
                title = title,
                description = description,
                imageUri = selectedImageUri,
                ingredients = ingredients.toList(),
                instructions = instructions.toList(),
                prepTime = prepTime,
                servings = servings,
                category = category,
                nutritionCalories = calories,
                nutritionProtein = protein,
                nutritionCarbs = carbs,
                nutritionFat = fat
            )
        }
    }

    private fun addIngredientChip(text: String) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                ingredients.remove(text)
                binding.chipGroupIngredients.removeView(this)
            }
        }
        binding.chipGroupIngredients.addView(chip)
    }

    private fun addInstructionStep(number: Int, text: String) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_instruction_step, binding.llInstructionSteps, false)

        val tvNumber = row.findViewById<android.widget.TextView>(R.id.tvStepNumber)
        val tvText = row.findViewById<android.widget.TextView>(R.id.tvStepText)
        val btnRemove = row.findViewById<android.widget.ImageView>(R.id.btnRemoveStep)

        tvNumber.text = number.toString()
        tvText.text = text
        btnRemove.setOnClickListener {
            val idx = instructions.indexOf(text)
            if (idx >= 0) instructions.removeAt(idx)
            binding.llInstructionSteps.removeView(row)
            renumberSteps()
        }

        binding.llInstructionSteps.addView(row)
    }

    private fun renumberSteps() {
        for (i in 0 until binding.llInstructionSteps.childCount) {
            val row = binding.llInstructionSteps.getChildAt(i)
            row.findViewById<android.widget.TextView>(R.id.tvStepNumber)?.text = (i + 1).toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
