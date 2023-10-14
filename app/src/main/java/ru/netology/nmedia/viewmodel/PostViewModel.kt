package ru.netology.nmedia.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.DateSeparator
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.random.Random

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorId = 0,
    likedByMe = false,
    likes = 0,
    authorAvatar = "",
    hidden = false,
    attachment = null,
    ownedByMe = false,
    published = LocalDateTime.now(),
)


private val today: LocalDateTime = LocalDateTime.now()


private val yesterday: LocalDateTime = today.minusDays(1)

private val weekAgo: LocalDateTime = today.minusDays(7)


fun Post?.isToday(): Boolean {
    if (this == null) return false

    return published.year == today.year && published.dayOfYear == today.dayOfYear
}


fun Post?.isYesterday(): Boolean {
    if (this == null) return false

    return published.year == yesterday.year && published.dayOfYear == yesterday.dayOfYear
}


fun Post?.isWeekAgo(): Boolean {
    if (this == null) return false

    return published.year == weekAgo.year && published.dayOfYear < weekAgo.dayOfYear
}

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    private val cached: Flow<PagingData<FeedItem>> = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators(
                terminalSeparatorType = TerminalSeparatorType.SOURCE_COMPLETE,
                generator = { before, after ->

                    when {
                        before == null && after.isToday() -> {
                            DateSeparator(DateSeparator.Type.TODAY)
                        }

                        (before == null && after.isYesterday()) || (before.isToday() && after.isYesterday()) -> {
                            DateSeparator(DateSeparator.Type.YESTERDAY)
                        }

                        before.isYesterday() && after.isWeekAgo() -> {
                            DateSeparator(DateSeparator.Type.WEEK_AGO)
                        }

                        else -> {
                            DateSeparator(DateSeparator.Type.WEEK_AGO)
                        }

                    }

                    if (before?.id?.rem(5) != 0L) null else
                        Ad(
                            Random.nextLong(),
                            "https://netology.ru",
                            "figma.jpg"
                        )

                }
            )
        }
        .cachedIn(viewModelScope)
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state
    val data: Flow<PagingData<FeedItem>> = appAuth.state
        .flatMapLatest { token ->
            cached.map { posts ->
                posts.map {
                    if (it is Post) {
                        it.copy(ownedByMe = it.authorId == token?.id)
                    } else {
                        it
                    }
                }
            }
        }.flowOn(Dispatchers.Default)

    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val newerCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerCount()
            .flowOn(Dispatchers.Default)
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
