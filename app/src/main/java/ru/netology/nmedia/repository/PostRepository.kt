package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data:Flow<List<Post>>
    fun getNewerCount(id: Long) : Flow<Int>
    suspend fun getAll()
    suspend fun getNewPost()

    suspend fun likeById(post: Post)

    suspend fun unlikeById(post: Post)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)


}
