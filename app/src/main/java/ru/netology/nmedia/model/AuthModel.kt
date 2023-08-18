package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class AuthModel(
    val id: Long = 0L,
    val token: String? = null,
)

data class AuthModelState(
    val isActing: Boolean = false,
    val error: Boolean = false,
    val response: AuthResponse = AuthResponse(),
)

data class AuthResponse(
    val code: Int = 0,
    val message: String? = null,
)



