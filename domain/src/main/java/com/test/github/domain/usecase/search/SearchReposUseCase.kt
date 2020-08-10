package com.test.github.domain.usecase.search

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
            val firstHalfResult = async {
                gitHubRepository.searchRepos(
                    token, params.copy(
                        page = params.page + 1
                    ), ids.take(15)
                )
            }.await()

            val secondHalfResult = async {
                gitHubRepository.searchRepos(
                    token, params.copy(
                        page = params.page + 2
                    ), ids.takeLast(15)
                )
            }.await()

            if (validateLimit(firstHalfResult, secondHalfResult)) {
                emit(Result.Failure(Error("You have reached the limit. Try again later.")))
            }

            validateResult(firstHalfResult, secondHalfResult, params.page)
        } ?: Result.Failure(Error("Not valid token")))
    }

    private fun validateLimit(firstHalfResult: Int, secondHalfResult: Int): Boolean {
        return firstHalfResult < 0 || secondHalfResult < 0
    }

    private fun validateResult(
        firstHalfResult: Int,
        secondHalfResult: Int,
        currentPage: Int
    ): SimpleResult {
        return if (firstHalfResult > 0 && secondHalfResult > 0) {
            Result.Success(SearchModel(currentPage + 2))
        } else if (firstHalfResult > 0 && secondHalfResult == 0) {
            Result.Success(SearchModel(currentPage + 1))
        } else {
            if (currentPage == 0) {
                Result.State.EMPTY
            } else {
                Result.Success(SearchModel(currentPage))
            }
        }
    }

    private fun generateIdsAsync() = mutableListOf<UUID>().apply {
        repeat(30) {
            add(UUID.randomUUID())
        }
    }

    private fun prepareStubs(ids: List<UUID>): List<SearchItem> {
        return ids.map { StubItem(it) }
    }
}