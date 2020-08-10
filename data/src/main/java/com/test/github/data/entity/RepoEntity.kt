package com.test.github.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "repos", indices = [Index("serverId")])
data class RepoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val starsCount: Int,
    val repoUrl: String,
    val ownerAvatarUrl: String,
    val ownerName: String,
    val serverId: Long
)