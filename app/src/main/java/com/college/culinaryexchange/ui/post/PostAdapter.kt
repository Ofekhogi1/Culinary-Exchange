package com.college.culinaryexchange.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.college.culinaryexchange.data.local.entity.PostEntity
import com.college.culinaryexchange.databinding.ItemPostBinding
import java.util.concurrent.TimeUnit

class PostAdapter(
    private val showActions: Boolean = false,
    private val onEdit: ((PostEntity) -> Unit)? = null,
    private val onDelete: ((PostEntity) -> Unit)? = null
) : ListAdapter<PostEntity, PostAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostEntity) {
            binding.tvUserName.text = post.userName
            binding.tvTitle.text = post.title
            binding.tvDescription.text = post.description
            binding.tvTimestamp.text = getRelativeTime(post.timestamp)

            if (post.imageUrl.isNotBlank()) {
                binding.ivPostImage.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(post.imageUrl)
                    .into(binding.ivPostImage)
            } else {
                binding.ivPostImage.visibility = View.GONE
            }

            if (showActions) {
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnEdit.setOnClickListener { onEdit?.invoke(post) }
                binding.btnDelete.setOnClickListener { onDelete?.invoke(post) }
            } else {
                binding.layoutActions.visibility = View.GONE
            }
        }
    }

    companion object {
        fun getRelativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
            val diff = now - timestamp
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
                diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
                else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7} weeks ago"
            }
        }

        private val DIFF = object : DiffUtil.ItemCallback<PostEntity>() {
            override fun areItemsTheSame(a: PostEntity, b: PostEntity) = a.id == b.id
            override fun areContentsTheSame(a: PostEntity, b: PostEntity) = a == b
        }
    }
}
