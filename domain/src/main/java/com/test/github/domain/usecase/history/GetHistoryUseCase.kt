package com.test.github.domain.usecase.history

import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.flow.flow

class GetHistoryUseCase(
    private val gitHubRepository: GitHubRepository
) : UseCase<Unit>() {

    override fun run(params: Unit) = flow<SimpleResult> {
        emit(Result.State.LOADING)
        emit(Result.Success(ReposModel(gitHubRepository.getHistory())))
    }
}