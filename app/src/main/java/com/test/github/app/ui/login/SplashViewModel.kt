package com.test.github.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.test.github.domain.model.Result
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import com.test.github.domain.usecase.account.GetAccountUseCase
import com.test.github.domain.usecase.repos.ClearReposUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SplashViewModel(
    getAccountUseCase: UseCase<Unit>,
    clearReposUseCase: UseCase<Unit>
) : ViewModel() {

    private val accountLiveData = getAccountUseCase.invoke(Unit).asLiveData()

    private val clearLiveData = clearReposUseCase.invoke(Unit).asLiveData()

    val eventLiveData: LiveData<SimpleResult> = MediatorLiveData<SimpleResult>().apply {

        var _accountResult: SimpleResult? = null
        var _clearResult: SimpleResult? = null

        fun update() {
            val accountResult = _accountResult ?: return
            _clearResult ?: return

            postValue(accountResult)
        }

        addSource(accountLiveData) {
            if (it is Result.Success) {
                _accountResult = it
                update()
            } else {
                postValue(it)
            }
        }

        addSource(clearLiveData) {
            _clearResult = it
            update()
        }
    }
}