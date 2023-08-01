package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepository.RepositoryCallback
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        // Начинаем загрузку
        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(object : RepositoryCallback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }

        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.RepositoryCallback<Post> {
                override fun onSuccess(value: Post) {
                    _postCreated.value = Unit
                }

                override fun onError(e: Exception) {
                    _data.value = FeedModel(error = true)
                }


            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(post: Post) {
        !post.likedByMe
        repository.likeByIdAsync(post, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(value: Post) {
                _data.value = FeedModel(posts = _data.value?.posts.orEmpty().map {
                    if (it.id == post.id) value else it
                })
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })

    }

    fun unlikeById(post: Post) {
        repository.unlikeByIdAsync(post, object : PostRepository.RepositoryCallback<Post> {
            override fun onSuccess(value: Post) {
                _data.value = FeedModel(posts = _data.value?.posts.orEmpty().map {
                    if (it.id == post.id) value else it
                })
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })

    }

    fun removeById(id: Long) {
        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()

        repository.removeByIdAsync(id, object : PostRepository.RepositoryCallback<Unit> {
            override fun onSuccess(value: Unit) {
                _data.value =
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )

            }

            override fun onError(e: Exception) {
                _data.value = _data.value?.copy(posts = old)
            }

        })


    }
}
