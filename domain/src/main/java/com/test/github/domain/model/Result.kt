package com.test.github.domain.model

sealed class Result<out T, out R> {
    data class Failure<out R : Throwable>(val errorData: R) : Result<Nothing, R>()
    data class Loading<out T>(val loadingData: T) : Result<T, Nothing>()
    data class Success<out T>(val successData: T) : Result<T, Nothing>()

    sealed class State : Result<Nothing, Nothing>() {
        object LOADING : State()
        object LOADED : State()
        object EMPTY : State()
        object NO_REPOS : State()
        object UNAUTHORIZED : State()

        object COOL_DOWN_RELEASED : State()
        object LIMIT_REACHED : State()
        object COOL_DOWN_START : State()
    }

    var hasBeenHandled = false
}