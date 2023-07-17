package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }



    override fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError()
                }
            })
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        val id = post.id
        val request: Request = if (post.likedByMe) {
            !post.likedByMe
            Request.Builder()
                .delete(gson.toJson(post).toRequestBody(jsonType))
        } else {
            !post.likedByMe
            Request.Builder()
                .post(gson.toJson(post).toRequestBody(jsonType))
        }
            .url("${BASE_URL}/api/posts/${id}/likes")
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError()
                }
            })


    }

    override fun saveAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/posts")
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        callback.onSuccess(gson.fromJson(body, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError()
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError()
                }
            })

    }

    override fun removeByIdAsync(id: Long,callback: PostRepository.RepositoryCallback<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id")
            .build()
        client.newCall(request)
            .enqueue(object : Callback{
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(Unit)
                    } catch (e: Exception) {
                        callback.onError()
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError()
                }

            })
    }
}
