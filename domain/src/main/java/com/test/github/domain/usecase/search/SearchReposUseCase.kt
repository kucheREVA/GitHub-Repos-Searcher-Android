package com.test.github.domain.usecase.search

import com.test.github.domain.data.RepoData
import com.test.github.domain.data.SearchData
import com.test.github.domain.item.SearchItem
import com.test.github.domain.item.StubItem
import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.model.SearchModel
import com.test.github.domain.model.SearchQuery
import com.test.github.domain.repository.AccountRepository
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import java.util.*

class SearchReposUseCase(
    private val gitHubRepository: GitHubRepository,
    private val accountRepository: AccountRepository
) : UseCase<SearchQuery>() {

    override fun run(params: SearchQuery) = flow<SimpleResult> {
        if (params.query.isBlank()) {
            emit(Result.State.EMPTY)
            return@flow
        }
        val ids = generateIdsAsync()
        emit(Result.Loading(ReposModel(prepareStubs(ids))))
        val token = accountRepository.getAuthToken()
        emit(token?.let {
            val firstHalf = searchReposAsync(token, FIRST_HALF_INCREMENT, params) {
                ids.take(HALF_CAPACITY)
            }

            val secondHalf = searchReposAsync(token, SECOND_HALF_INCREMENT, params) {
                ids.takeLast(HALF_CAPACITY)
            }

            val firstHalfResult = firstHalf.await()
            val secondHalfResult = secondHalf.await()

            validateResult(firstHalfResult, secondHalfResult, ids, params.page)
        } ?: Result.Failure(Error("Not valid token")))
    }

    private suspend fun searchReposAsync(
        token: String,
        pageIncrement: Int,
        params: SearchQuery,
        action: () -> List<UUID>
    ) = async {
        val searchParams = params.copy(page = params.page + pageIncrement)
        gitHubRepository.searchRepos(token, searchParams, action.invoke())
    }

    private suspend fun validateResult(
        firstHalfResult: SearchData?,
        secondHalfResult: SearchData?,
        ids: List<UUID>,
        currentPage: Int
    ): SimpleResult {
        val items = mutableListOf<RepoData>()
        return when {
            firstHalfResult == null -> {
                Result.State.COOL_DOWN_START
            }
            firstHalfResult.items.isEmpty() -> {
                Result.State.LIMIT_REACHED
            }
            secondHalfResult == null -> {
                items.addAll(firstHalfResult.items)
                gitHubRepository.saveRepos(ids, items)
                Result.Success(
                    SearchModel(
                        page = currentPage + FIRST_HALF_INCREMENT,
                        coolDownEnabled = true
                    )
                )
            }
            secondHalfResult.items.isEmpty() -> {
                items.addAll(firstHalfResult.items)
                gitHubRepository.saveRepos(ids, items)
                Result.Success(
                    SearchModel(
                        page = currentPage + FIRST_HALF_INCREMENT
                    )
                )
            }
            else -> {
                items.addAll(firstHalfResult.items)
                items.addAll(secondHalfResult.items)
                gitHubRepository.saveRepos(ids, items)
                Result.Success(
                    SearchModel(
                        page = currentPage + SECOND_HALF_INCREMENT
                    )
                )
            }
        }
    }

    private fun generateIdsAsync() = mutableListOf<UUID>().apply {
        repeat(LOAD_CAPACITY) {
            add(UUID.randomUUID())
        }
    }

    private fun prepareStubs(ids: List<UUID>): List<SearchItem> {
        return ids.map { StubItem(it) }
    }

    companion object {
        const val FIRST_HALF_INCREMENT = 1
        const val SECOND_HALF_INCREMENT = 2
        const val HALF_CAPACITY = 15
        const val LOAD_CAPACITY = 30
    }
}