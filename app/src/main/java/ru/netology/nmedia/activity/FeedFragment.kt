package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,Bundle().apply {
                    textArg = post.content
                })
                viewModel.edit(post)
            }

            override fun onViewImage(post: Post){
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    Bundle().apply {
                        textArg = post.attachment!!.url
                    })
            }

            override fun onLike(post: Post) {
                if (!post.likedByMe){
                    viewModel.likeById(post)
                } else viewModel.unlikeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onRefresh() {
                viewModel.loadPosts()
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })
        binding.list.adapter = adapter

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.swiperefresh.isRefreshing = false

            if (state.error) {
                val message = if (state.response.code == 0) {
                    getString(R.string.error_loading)
                } else {
                    getString(
                        R.string.error_response,
                        state.response.message.toString(),
                        state.response.code
                    )
                }
                Snackbar.make(
                    binding.root,
                    message,
                    Snackbar.LENGTH_LONG
                ).setAction(android.R.string.ok) {
                    return@setAction
                }.show()
            }
        }
        viewModel.date.observe(viewLifecycleOwner){
            binding.emptyText.isVisible = it.empty
            adapter.submitList(it.posts)
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.fabNewer.hide()
            } else {
                binding.fabNewer.text = getString(R.string.newer, it)
                binding.fabNewer.show()
            }
        }


        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.fabNewer.setOnClickListener {
            viewModel.loadNewPosts()
            binding.fabNewer.hide()
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        return binding.root
    }
}
