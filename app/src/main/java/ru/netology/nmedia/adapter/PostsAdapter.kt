package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onDislike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onViewImage(post: Post) {}
    fun onShare(post: Post) {}
    fun onRefresh() {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            // в адаптере
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            val url = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
            Glide.with(avatar)
                .load(url)
                .placeholder(R.drawable.ic_more_vert_24)
                .error(R.drawable.error_24)
                .circleCrop()
                .timeout(10_000)
                .into(avatar)

            if (post.attachment?.type == AttachmentType.IMAGE) {
                image.visibility = View.VISIBLE
                Glide.with(image)
                    .load("http://10.0.2.2:9999/media/${post.attachment.url}")
                    .timeout(10_000)
                    .into(image)

            } else {
                image.visibility = View.GONE
            }

            image.setOnClickListener {
                onInteractionListener.onViewImage(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)

            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
