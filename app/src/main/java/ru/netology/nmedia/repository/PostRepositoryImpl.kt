package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import retrofit2.Call
import retrofit2.Callback
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.ApiError


class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
    }

    override suspend fun getAll() {
        val response = PostApi.service.getPosts()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto) )
    }

    override suspend fun likeById(post: Post) {
        val response = PostApi.service.likeById(post.id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val posts = response.body() ?: throw RuntimeException("Body is empty")
        dao.likeById(posts.id)
    }

    override suspend fun unlikeById(post: Post) {
        val response = PostApi.service.unlikeById(post.id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val posts = response.body() ?: throw RuntimeException("Body is empty")
        dao.likeById(posts.id)
    }

    override suspend fun save(post: Post) {
        val response = PostApi.service.savePosts(post)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val posts = response.body() ?: throw RuntimeException("Body is empty")
        dao.save(PostEntity.fromDto(posts))
    }

    override suspend fun removeById(id: Long) {
        val response = PostApi.service.deletePost(id)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        val posts = response.body() ?: throw RuntimeException("Body is empty")
        dao.removeById(id)
    }
}
