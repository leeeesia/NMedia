package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {

    fun getAllAsync(callback: RepositoryCallback<List<Post>>)
    fun likeByIdAsync(post: Post, callback: RepositoryCallback<Post>)
    fun saveAsync(post: Post, callback: RepositoryCallback<Post>)
    fun removeByIdAsync(id: Long, callback: RepositoryCallback<Unit>)

    interface RepositoryCallback<T> {
        fun onSuccess(value: T)
        fun onError()
    }

}
