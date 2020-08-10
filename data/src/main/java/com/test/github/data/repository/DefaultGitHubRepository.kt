package com.test.github.data.repository

import com.test.github.data.database.AppDatabase
import com.test.github.data.entity.HistoryEntity
import com.test.github.data.entity.RepoEntity
import com.test.github.data.remote.network.GitHubApiService
import com.test.github.domain.data.SearchData
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.SearchQuery
import com.test.github.domain.repository.GitHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.*

class DefaultGitHubRepository(
    private val gitHubApi: GitHubApiService,
    private val database: AppDatabase
) : GitHubRepository {

    override suspend fun getHistory(): List<RepoItem> {
        return database.getReposDao().getHistory().map {
            RepoItem(
                itemId = UUID(it.serverId, it.serverId),
                name = it.name,
                description = it.description,
                starsCount = it.starsCount,
                repoUrl = it.repoUrl,
                ownerName = it.ownerName,
                ownerAvatarUrl = it.ownerAvatarUrl,
                serverId = it.serverId,
                viewedTimestamp = it.viewedTimestamp
            )
        }
    }

    override suspend fun updateViewedTimestamp(item: RepoItem) {
        return database.getReposDao().updateHistory(
            HistoryEntity(
                serverId = item.serverId,
                name = item.name,
                description = item.description,
                starsCount = item.starsCount,
                repoUrl = item.repoUrl,
                ownerName = item.ownerName,
                ownerAvatarUrl = item.ownerAvatarUrl,
                viewedTimestamp = item.viewedTimestamp
            )
        )
    }

    override suspend fun clearRepos(): Boolean {
        return database.getReposDao().clearRepos() > 0
    }

    override fun getRepos(): Flow<List<RepoItem>> {
        return database.getReposDao().getRepos()
            .flowOn(Dispatchers.IO)
            .map { mapEntityToItem(it) }
    }

    override suspend fun searchRepos(
        token: String,
        searchQuery: SearchQuery,
        ids: List<UUID>
    ): Int {
        val response = gitHubApi.searchRepos(
            token,
            searchQuery.page,
            searchQuery.query.replace(" ", "+"),
            searchQuery.sort,
            searchQuery.order
        )

        return if (response.isSuccessful) {
            val data = response.body()
            saveToMemory(ids, data).size
        } else {
            -1
        }
    }

    private suspend fun saveToMemory(ids: List<UUID>, data: SearchData?): List<Long> {
        return data?.let {
            database.getReposDao().insertRepos(mapDataToEntity(ids, it))
        } ?: emptyList()
    }

    private fun mapEntityToItem(entities: List<RepoEntity>): List<RepoItem> {
        return entities.map {
            RepoItem(
                itemId = UUID.fromString(it.id),
                name = it.name,
                description = it.description,
                starsCount = it.starsCount,
                repoUrl = it.repoUrl,
                ownerName = it.ownerName,
                ownerAvatarUrl = it.ownerAvatarUrl,
                serverId = it.serverId
            )
        }
    }

    private fun mapDataToEntity(ids: List<UUID>, data: SearchData): List<RepoEntity> {
        return data.items.mapIndexed { index, repoData ->
            RepoEntity(
                id = ids[index].toString(),
                name = repoData.name,
                description = repoData.description,
                starsCount = repoData.stargazers_count,
                repoUrl = repoData.html_url,
                ownerName = repoData.owner.login,
                ownerAvatarUrl = repoData.owner.avatar_url,
                serverId = repoData.id
            )
        }
    }
}