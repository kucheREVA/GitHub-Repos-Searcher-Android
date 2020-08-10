package com.test.github.domain.data

data class SearchData(
    val incomplete_results: Boolean,
    val items: List<RepoData>,
    val total_count: Int
)