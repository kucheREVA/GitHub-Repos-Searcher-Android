package com.test.github.domain.usecase.history

import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class GetHistoryUseCase(
    private val gitHubRepository: GitHubRepository
) : UseCase<Unit>() {

    override suspend fun onStart(params: Unit): SimpleResult? {
        return Result.State.LOADING
    }

    override fun run(params: Unit) = gitHubRepository.getHistory()
        .flowOn(Dispatchers.IO)
        .map { Result.Success(ReposModel(it)) }
}