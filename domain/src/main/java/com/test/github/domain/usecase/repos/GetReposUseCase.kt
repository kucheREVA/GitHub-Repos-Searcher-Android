package com.test.github.domain.usecase.repos

import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetReposUseCase(
    private val gitHubRepository: GitHubRepository
) : UseCase<Unit>() {

    companion object {
        private const val FIRST = 1
    }

    override fun run(params: Unit): Flow<SimpleResult> {
        return gitHubRepository.getRepos()
            .drop(FIRST)
            .flowOn(Dispatchers.Default)
            .map {
                if (it.isNotEmpty()) {
                    Result.Success(ReposModel(it))
                } else {
                    Result.State.EMPTY
                }
            }
    }
}