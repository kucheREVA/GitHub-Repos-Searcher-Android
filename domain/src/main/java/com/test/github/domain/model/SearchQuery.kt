package com.test.github.domain.model

data class SearchQuery(
    val query: String,
    val page: Int,
    val sort: String = "stars",
    val order: String = "desc"
)