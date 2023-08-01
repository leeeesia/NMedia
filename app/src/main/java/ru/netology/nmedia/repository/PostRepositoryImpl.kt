package ru.netology.nmedia.repository


import android.content.Context
import android.widget.Toast
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import java.io.IOException


class PostRepositoryImpl : PostRepository {


    override fun getAllAsync(callback: PostRepository.RepositoryCallback<List<Post>>) {
        PostApi.service
            .getPosts()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: retrofit2.Response<List<Post>>,
                ) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(RuntimeException("Body is empty"))
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })
    }

    override fun likeByIdAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        val id = post.id
        PostApi.service.likeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: retrofit2.Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val post = response.body()
                    if (post == null) {
                        callback.onError(RuntimeException("Body is empty"))
                        return
                    }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })


    }

    override fun unlikeByIdAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {
        val id = post.id
        PostApi.service.unlikeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: retrofit2.Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val post = response.body()
                    if (post == null) {
                        callback.onError(RuntimeException("Body is empty"))
                        return
                    }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })


    }

    override fun saveAsync(post: Post, callback: PostRepository.RepositoryCallback<Post>) {

        PostApi.service
            .savePosts(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: retrofit2.Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val post = response.body()
                    if (post == null) {
                        callback.onError(RuntimeException("Body is empty"))
                        return
                    }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })

    }


    override fun removeByIdAsync(id: Long, callback: PostRepository.RepositoryCallback<Unit>) {
        PostApi.service
            .deletePost(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: retrofit2.Response<Unit>) {
                    callback.onSuccess(Unit)
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }


            })
    }
}
