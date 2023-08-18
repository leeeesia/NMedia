package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val hidden: Boolean,
    val attachment : Attachment?,
    val ownedByMe: Boolean = false,
)

data class Attachment(
    val url: String,
    val type: AttachmentType
)

