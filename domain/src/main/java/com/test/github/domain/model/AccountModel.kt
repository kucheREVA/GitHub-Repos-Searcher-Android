package com.test.github.domain.model

data class AccountModel(
    val accountUserId: String,
    val accountEmail: String,
    val authToken: String,
    val idToken: String,
    val secret: String,
    val tokenProvider: String
) : Model