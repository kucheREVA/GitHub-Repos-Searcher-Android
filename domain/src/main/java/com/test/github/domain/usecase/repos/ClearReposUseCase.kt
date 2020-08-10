package com.test.github.domain.usecase.repos

import com.test.github.domain.model.None
import com.test.github.domain.model.Result
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.flow.flow

class ClearReposUseCase(
    private val gitHubRepository: GitHubRepository
) : UseCase<Unit>() {

    override fun run(params: Unit) = flow<SimpleResult> {
        emit(
            if (gitHubRepository.clearRepos()) {
                Result.Success(None)
            } else {
                Result.State.NO_REPOS
            }
        )
    }
}