package com.test.github.domain.usecase.history

import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.None
import com.test.github.domain.model.Result
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.flow.flow

class UpdateHistoryUseCase(
    private val gitHubRepository: GitHubRepository
) : UseCase<RepoItem>() {

    override fun run(params: RepoItem) = flow<SimpleResult> {
        gitHubRepository.updateViewedTimestamp(
            params.copy(viewedTimestamp = System.currentTimeMillis(), isViewed = true)
        )

        emit(Result.Success(None))
    }
}