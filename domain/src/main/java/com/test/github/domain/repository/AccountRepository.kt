package com.test.github.domain.repository

import com.test.github.domain.model.AccountModel

interface AccountRepository {

    fun getAuthToken(): String?

    suspend fun updateAuthToken(accountModel: AccountModel): Boolean

    suspend fun createUserAccount(accountModel: AccountModel): Boolean

    suspend fun getUserAccount(): AccountModel?

    suspend fun removeUserAccount(): Boolean
}