package com.college.culinaryexchange.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.college.culinaryexchange.databinding.ItemQuoteBinding

class QuoteAdapter(
    private var quote: Quote,
    private val onRefresh: () -> Unit
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(quote)

    override fun getItemCount() = 1

    fun updateQuote(newQuote: Quote) {
        quote = newQuote
        notifyItemChanged(0)
    }

    inner class ViewHolder(private val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(q: Quote) {
            binding.tvQuote.text = "\"${q.text}\""
            binding.tvQuoteAuthor.text = "— ${q.author}"
            binding.ivRefreshQuote.setOnClickListener { onRefresh() }
        }
    }
}
