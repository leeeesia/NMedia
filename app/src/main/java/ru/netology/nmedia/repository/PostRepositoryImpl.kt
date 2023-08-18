package ru.netology.nmedia.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.ApiError
import ru.netology.nmedia.util.NetworkError
import ru.netology.nmedia.util.UnknownError
import java.io.File
import java.io.IOException
import java.util.concurrent.CancellationException


class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {
    override val data: Flow<List<Post>> = dao.getAllVisible().map {
        it.map(PostEntity::toDto)
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                kotlinx.coroutines.delay(10_000)
                val response = PostApi.service.getNewer(id)

                val posts = response.body().orEmpty()

                dao.insertShadow(posts.toEntity(true)) //здесь посты с совпадающим id в базе данных  не будут заменены

                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override suspend fun getAll() {
        val response = PostApi.service.getPosts()
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        val posts = response.body() ?: throw RuntimeException("Body is empty")

        dao.insert(posts.map(PostEntity::fromDto))
    }

    override fun getNewPost() {
        dao.getNewPost()
    }


    override suspend fun likeById(post: Post) {
        try {
            dao.likeById(post.id)
            val response = PostApi.service.likeById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw UnknownError
        }
    }

    override suspend fun unlikeById(post: Post) {
        try {
            dao.likeById(post.id)
            val response = PostApi.service.unlikeById(post.id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            dao.likeById(post.id)
            throw UnknownError
        }

    }

    override suspend fun save(post: Post) {
        try {
            val response = PostApi.service.savePosts(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            val media = uploadMedia(file)

            val response = PostApi.service.savePosts(
                post.copy(
                    attachment = Attachment(
                        url = media.id,
                        type = AttachmentType.IMAGE
                    )
                )
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun uploadMedia(file: File): Media {
        val formData = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        val response = PostApi.service.uploadMedia(formData)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = PostApi.service.deletePost(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun signIn(login: String, password: String): AuthModel {
        val response = PostApi.service.updateUser(login, password)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun signUp(name: String, login: String, password: String): AuthModel {
        val response = PostApi.service.registerUser(login, password, name)

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return response.body() ?: throw ApiError(response.code(), response.message())
    }


}
