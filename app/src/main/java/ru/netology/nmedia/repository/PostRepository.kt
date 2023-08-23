package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File
import ru.netology.nmedia.model.AuthModel

interface PostRepository {
    val data: Flow<PagingData<Post>>
    fun getNewerCount(): Flow<Int>
    suspend fun getAll()
    fun getNewPost()

    suspend fun likeById(post: Post)

    suspend fun unlikeById(post: Post)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun removeById(id: Long)

    suspend fun signIn(login: String, password: String): AuthModel

    suspend fun signUp(login: String, password: String, name: String): AuthModel


}
