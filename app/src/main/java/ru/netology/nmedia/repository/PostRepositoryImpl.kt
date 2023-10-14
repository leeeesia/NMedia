package ru.netology.nmedia.repository


import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.FeedItem
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
import javax.inject.Inject
import kotlin.random.Random


class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = dao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
        )
    ).flow
        .map { pagingData ->
            pagingData.map(PostEntity::toDto)
            //it.map(PostEntity::toDto)
            //    .insertSeparators { previous, _ ->
            //        if (previous?.id?.rem(5) == 0L) {
            //             Ad(Random.nextLong(),"figma.jpg" )
            //        } else{
            //        null
            //    }
            //    }
        }

    override fun getNewerCount(): Flow<Int> = flow {
        while (true) {
            try {
                kotlinx.coroutines.delay(10_000)
                val id = if (dao.isEmpty()) 0L else dao.getLateId()
                val response = apiService.getNewer(id)

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
        val response = apiService.getPosts()
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
            val response = apiService.likeById(post.id)
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
            val response = apiService.unlikeById(post.id)
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
            val response = apiService.savePosts(post)
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

            val response = apiService.savePosts(
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

        val response = apiService.uploadMedia(formData)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = apiService.deletePost(id)
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
        val response = apiService.updateUser(login, password)
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return response.body() ?: throw ApiError(response.code(), response.message())
    }

    override suspend fun signUp(name: String, login: String, password: String): AuthModel {
        val response = apiService.registerUser(login, password, name)

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return response.body() ?: throw ApiError(response.code(), response.message())
    }


}
