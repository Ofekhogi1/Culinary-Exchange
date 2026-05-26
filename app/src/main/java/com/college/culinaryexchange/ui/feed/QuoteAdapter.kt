package com.college.culinaryexchange.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.college.culinaryexchange.databinding.ItemQuoteBinding

class QuoteAdapter(
    private val onRefresh: () -> Unit
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>() {

    private var meal: MealInspiration? = null
    private var isLoading: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(meal, isLoading)

    override fun getItemCount() = 1

    fun updateMeal(newMeal: MealInspiration?) {
        meal = newMeal
        notifyItemChanged(0)
    }

    fun updateLoading(loading: Boolean) {
        isLoading = loading
        notifyItemChanged(0)
    }

    inner class ViewHolder(private val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: MealInspiration?, loading: Boolean) {
            binding.progressMeal.visibility = if (loading) View.VISIBLE else View.GONE
            binding.ivRefreshQuote.visibility = if (loading) View.INVISIBLE else View.VISIBLE

            when {
                meal != null -> {
                    binding.tvQuote.text = meal.name
                    binding.tvQuoteAuthor.text = "${meal.category} · ${meal.area} cuisine"
                    Glide.with(binding.root.context)
                        .load(meal.thumbnailUrl)
                        .centerCrop()
                        .into(binding.ivMealThumb)
                    binding.ivMealThumb.visibility = View.VISIBLE
                }
                !loading -> {
                    binding.tvQuote.text = "Tap ↻ for meal inspiration"
                    binding.tvQuoteAuthor.text = "Powered by TheMealDB"
                    binding.ivMealThumb.visibility = View.GONE
                }
            }

            binding.ivRefreshQuote.setOnClickListener { onRefresh() }
        }
    }
}
