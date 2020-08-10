package com.test.github.domain.usecase.account

import com.test.github.domain.model.Result
import com.test.github.domain.repository.AccountRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class GetAccountUseCase(
    private val accountRepository: AccountRepository
) : UseCase<Unit>() {

    override suspend fun onStart(params: Unit): SimpleResult? {
        return Result.State.LOADING
    }

    override fun run(params: Unit) = flow<SimpleResult> {
        delay(2_000)
        emit(accountRepository.getUserAccount()?.let {
            Result.Success(it)
        } ?: Result.State.UNAUTHORIZED)
    }
}