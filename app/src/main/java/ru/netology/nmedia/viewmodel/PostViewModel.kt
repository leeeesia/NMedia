package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    hidden = false,
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao()
    )

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    val date: LiveData<FeedModel> = repository.data.map {
        FeedModel(posts = it, empty = it.isEmpty())
    }.asLiveData(Dispatchers.Default)
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    val newerCount: LiveData<Int> = date.switchMap {
        val id = it.posts.firstOrNull()?.id ?: 0L

        repository.getNewerCount(id).asLiveData(Dispatchers.Default)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }

    fun setPhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun loadPosts() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getAll()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun loadNewPosts() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(loading = true))
            try {
                repository.getNewPost()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun refresh() {
        // Начинаем загрузку
        viewModelScope.launch {
            _state.postValue(FeedModelState(refreshing = true))
            try {
                repository.getAll()
                _state.postValue(FeedModelState())
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                try {
                    _photo.value?.let { photoModel ->
                        repository.saveWithAttachment(it, photoModel.file)
                    } ?: run {
                        repository.save(it)
                    }
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
            edited.value = empty
        }

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
        viewModelScope.launch {
            !post.likedByMe
            try {
                repository.likeById(post)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun unlikeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.unlikeById(post)
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)

            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }

    }
}
