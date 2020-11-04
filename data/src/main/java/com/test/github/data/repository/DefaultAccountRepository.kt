package com.test.github.data.repository

import android.accounts.Account
import android.accounts.AccountManager
import androidx.core.os.bundleOf
import com.test.github.domain.model.AccountModel
import com.test.github.domain.repository.AccountRepository

class DefaultAccountRepository(
    private val accountManager: AccountManager
) : AccountRepository {

    companion object {
        private const val ACCOUNT_TYPE = "com.test.github.app"

        private const val ACCOUNT_ID = "com.test.github.app.uid"
        private const val ACCOUNT_EMAIL = "com.test.github.app.email"
        private const val ACCOUNT_AUTH_TOKEN = "com.test.github.app.token"
        private const val ACCOUNT_TOKEN_PROVIDER = "com.test.github.app.token.provider"
        private const val ACCOUNT_ID_TOKEN = "com.test.github.app.token.id"
        private const val ACCOUNT_TOKEN_SECRET = "com.test.github.app.token.secret"
    }

    override fun getAuthToken(): String? {
        return getAccount()?.let {
            accountManager.getUserData(it, ACCOUNT_AUTH_TOKEN)
        }
    }

    override suspend fun updateAuthToken(accountModel: AccountModel): Boolean {
        return getAccount()?.let {
            accountManager.setUserData(it, ACCOUNT_TOKEN_PROVIDER, accountModel.tokenProvider)
            accountManager.setAuthToken(it, ACCOUNT_AUTH_TOKEN, accountModel.authToken)
            accountManager.setAuthToken(it, ACCOUNT_ID_TOKEN, accountModel.idToken)
            accountManager.setAuthToken(it, ACCOUNT_TOKEN_SECRET, accountModel.secret)

            return@let true
        } ?: false
    }

    override suspend fun createUserAccount(accountModel: AccountModel): Boolean {
        val bundle = bundleOf(
            ACCOUNT_ID to accountModel.accountUserId,
            ACCOUNT_EMAIL to accountModel.accountEmail,
            ACCOUNT_TOKEN_PROVIDER to accountModel.tokenProvider,
            ACCOUNT_AUTH_TOKEN to accountModel.authToken,
            ACCOUNT_ID_TOKEN to accountModel.idToken,
            ACCOUNT_TOKEN_SECRET to accountModel.secret
        )

        val account = Account(accountModel.accountEmail, ACCOUNT_TYPE)

        return accountManager.addAccountExplicitly(account, accountModel.tokenProvider, bundle)
    }

    override suspend fun getUserAccount(): AccountModel? {
        return getAccount()?.let {
            AccountModel(
                accountEmail = accountManager.getUserData(it, ACCOUNT_EMAIL),
                accountUserId = accountManager.getUserData(it, ACCOUNT_ID),
                authToken = accountManager.getUserData(it, ACCOUNT_AUTH_TOKEN),
                idToken = accountManager.getUserData(it, ACCOUNT_ID_TOKEN),
                secret = accountManager.getUserData(it, ACCOUNT_TOKEN_SECRET),
                tokenProvider = accountManager.getUserData(it, ACCOUNT_TOKEN_PROVIDER)
            )
        }
    }

    override suspend fun removeUserAccount(): Boolean {
        return getAccount()?.let {
            accountManager.removeAccountExplicitly(it)
        } ?: false
    }

    private fun getAccount() = accountManager.getAccountsByType(ACCOUNT_TYPE).firstOrNull()
}