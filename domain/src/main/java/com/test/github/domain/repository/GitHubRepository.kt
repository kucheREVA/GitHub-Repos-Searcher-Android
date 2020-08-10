package com.test.github.domain.repository

import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.SearchQuery
import kotlinx.coroutines.flow.Flow
import java.util.*

interface GitHubRepository {

    suspend fun updateViewedTimestamp(item: RepoItem)

    suspend fun getHistory(): List<RepoItem>

    suspend fun clearRepos(): Boolean

    suspend fun searchRepos(
        token: String,
        searchQuery: SearchQuery,
        ids: List<UUID>
    ): Int

    fun getRepos(): Flow<List<RepoItem>>
}