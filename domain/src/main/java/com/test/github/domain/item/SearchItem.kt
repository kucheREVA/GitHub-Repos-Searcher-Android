package com.test.github.domain.item

import java.util.*


sealed class SearchItem(open val itemId: UUID)

data class RepoItem(
    override val itemId: UUID,
    val name: String,
    val description: String?,
    val starsCount: Int,
    val repoUrl: String,
    val ownerName: String,
    val ownerAvatarUrl: String?,
    val serverId: Long,
    val isViewed: Boolean = false,
    val viewedTimestamp: Long = 0
) : SearchItem(itemId)

data class StubItem(
    override val itemId: UUID
) : SearchItem(itemId)

data class CooldownItem(
    val coolDownValue: String
) : SearchItem(UUID.fromString("00000000-0000-0000-0000-000000000000"))