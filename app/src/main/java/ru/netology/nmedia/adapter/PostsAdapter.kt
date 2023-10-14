package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardDateBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
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
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    private val typeAd = 0
    private val typePost = 1
    private val typeDate = 2
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is DateSeparator -> typeDate
            else -> error("unknown item type")
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            typeDate -> DateViewHolder(
                CardDateBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )

            else -> error("unknown item type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is DateSeparator -> (holder as? DateViewHolder)?.bind(item)
            else -> error("unknown item type")
        }
    }


    class AdViewHolder(
        private val binding: CardAdBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: Ad) {
            fun bind(ad: Ad) {
                Glide.with(binding.image)
                    .load("http://10.0.2.2:9999/media/${ad.image}")
                    .timeout(10_000)
                    .into(binding.image)
            }
        }
    }

    class PostViewHolder(
        private val binding: CardPostBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                author.text = post.author
                content.text = post.content
                published.text = post.published.toString()
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

                menu.isVisible = post.ownedByMe

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

    class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }
    }

    class DateViewHolder(
        private val binding: CardDateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(date: DateSeparator) {
            val resource = when (date.type) {
                DateSeparator.Type.TODAY -> R.string.today
                DateSeparator.Type.YESTERDAY -> R.string.yesterday
                DateSeparator.Type.WEEK_AGO -> R.string.week_ago
            }

            binding.root.setText(resource)
        }
    }
}

