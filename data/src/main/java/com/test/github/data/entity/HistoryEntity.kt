package com.test.github.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.*

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val serverId: Long,
    val name: String,
    val description: String?,
    val starsCount: Int,
    val repoUrl: String,
    val ownerAvatarUrl: String?,
    val ownerName: String,
    val viewedTimestamp: Long
)