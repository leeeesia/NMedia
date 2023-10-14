package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId:Long,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    val hidden: Boolean = false,
    val ownedByMe:Boolean = false,
    @Embedded
    var attachment: Attachment?,
) {
    fun toDto() = Post(id, author, authorId,content, LocalDateTime.ofInstant(Instant.ofEpochSecond(published), ZoneId.systemDefault()), likedByMe, likes, authorAvatar, hidden, attachment, ownedByMe)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorId,
                dto.content,
                dto.published.atZone(ZoneId.systemDefault()).toEpochSecond(),
                dto.likedByMe,
                dto.likes,
                dto.authorAvatar,
                dto.hidden,
                dto.ownedByMe,
                dto.attachment
            )

    }
}
fun List<PostEntity>.toDto():List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map(PostEntity::fromDto).map{
    it.copy(hidden = hidden)
}

