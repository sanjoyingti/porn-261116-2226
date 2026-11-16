package com.aibuilt.porn.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aibuilt.porn.model.TorLink
import com.aibuilt.porn.model.VideoItem

@Entity(tableName = "catalog")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val duration: Int,
    val source: String,
    val embed: String,
    val category: String,
    val age: Int?,
    val views: Long,
    val date: Long,
    val thumb: String?
) {
    fun toModel() = VideoItem(id, title, duration, source, embed, category, age, views, date, thumb)
}

fun VideoItem.toEntity() = VideoEntity(id, title, duration, source, embed, category, age, views, date, thumb)

@Entity(tableName = "tor_links")
data class TorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val url: String,
    val groupName: String
) {
    fun toModel() = TorLink(id, name, description, url, groupName)
}

fun TorLink.toEntity() = TorEntity(id, name, description, url, group)

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val id: String, val addedAt: Long)

@Entity(tableName = "hidden")
data class HiddenEntity(@PrimaryKey val id: String)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(@PrimaryKey val query: String, val searchedAt: Long)

@Entity(tableName = "dead_links")
data class DeadLinkEntity(@PrimaryKey val id: String, val reportedAt: Long)
