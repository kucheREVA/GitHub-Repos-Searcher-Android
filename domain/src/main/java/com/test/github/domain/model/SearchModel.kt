package com.test.github.domain.model

data class SearchModel(
    val page: Int,
    val coolDownEnabled: Boolean = false
) : Model