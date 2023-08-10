package ru.netology.nmedia.util

sealed class Error(var code: String): RuntimeException()
class ApiError(val status: Int, code: String): Error(code)
