package ru.netology.nmedia.util

sealed class Error(var code: String): RuntimeException()
class ApiError(val status: Int, code: String): Error(code)
object NetworkError : Error("error_network")
object UnknownError: Error("error_unknown")
