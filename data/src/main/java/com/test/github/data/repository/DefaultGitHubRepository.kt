package com.test.github.data.repository

import com.test.github.data.database.AppDatabase
import com.test.github.data.entity.HistoryEntity
import com.test.github.data.entity.RepoEntity
import com.test.github.data.remote.network.GitHubApiService
import com.test.github.domain.data.RepoData
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

    override fun getHistory(): Flow<List<RepoItem>> {
        return database.getReposDao().getHistory()
            .map { it.mapToRepoItem() }
    }

    override fun getRepos(): Flow<List<RepoItem>> {
        return database.getReposDao().getRepos()
            .flowOn(Dispatchers.IO)
            .map { it.mapToItem() }
    }

    override suspend fun updateViewedTimestamp(item: RepoItem) {
        return database.getReposDao().updateHistory(item.mapToHistoryEntity())
    }

    override suspend fun clearRepos(): Boolean {
        return database.getReposDao().clearRepos() > 0
    }

    override suspend fun saveRepos(ids: List<UUID>, items: List<RepoData>) {
        val entities = items.mapToEntity(ids)
        database.getReposDao().insertRepos(entities)
    }

    override suspend fun searchRepos(
        searchQuery: SearchQuery,
        ids: List<UUID>
    ): SearchData? {
        val response = gitHubApi.searchRepos(
            searchQuery.page,
            prepareQuery(searchQuery.query),
            searchQuery.sort,
            searchQuery.order
        )

        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    private fun prepareQuery(query: String): String {
        return "${query.replace(" ", "+")}+in:name"
    }

    private fun List<RepoEntity>.mapToItem() = map {
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

    private fun List<RepoData>.mapToEntity(ids: List<UUID>) = mapIndexed { index, repoData ->
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


    private fun List<HistoryEntity>.mapToRepoItem() = map {
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

    private fun RepoItem.mapToHistoryEntity() = HistoryEntity(
        serverId = serverId,
        name = name,
        description = description,
        starsCount = starsCount,
        repoUrl = repoUrl,
        ownerName = ownerName,
        ownerAvatarUrl = ownerAvatarUrl,
        viewedTimestamp = viewedTimestamp
    )

}