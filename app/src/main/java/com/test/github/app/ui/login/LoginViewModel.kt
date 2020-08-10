package com.test.github.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.OAuthCredential
import com.test.github.domain.model.AccountModel
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import com.test.github.domain.usecase.account.CreateAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

class LoginViewModel(
    private val createAccountUseCase: UseCase<AccountModel>
) : ViewModel() {

    private val _loginLiveData = MutableLiveData<SimpleResult>()
    val loginLiveData: LiveData<SimpleResult> = _loginLiveData

    @ExperimentalCoroutinesApi
    fun onAuthSuccess(authResult: AuthResult) = runBlocking {
        val oAuthCredential = authResult.credential as? OAuthCredential
        val account = AccountModel(
            accountEmail = authResult.user?.email ?: "",
            accountUserId = authResult.user?.uid ?: "",
            authToken = oAuthCredential?.accessToken ?: "",
            idToken = oAuthCredential?.idToken ?: "",
            secret = oAuthCredential?.secret ?: "",
            tokenProvider = authResult.credential?.provider ?: ""
        )
        createAccountUseCase.invoke(account).collect {
            _loginLiveData.postValue(it)
        }
    }
}