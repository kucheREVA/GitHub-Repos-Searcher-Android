package com.test.github.domain.usecase.account

import com.test.github.domain.model.AccountModel
import com.test.github.domain.model.None
import com.test.github.domain.model.Result
import com.test.github.domain.repository.AccountRepository
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.flow.flow

class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) : UseCase<AccountModel>() {

    override fun run(params: AccountModel) = flow<SimpleResult> {
        emit(
            if (accountRepository.createUserAccount(params)) {
                Result.Success(None)
            } else {
                Result.State.UNAUTHORIZED
            }
        )
    }
}