package com.test.github.domain.model

import com.test.github.domain.item.SearchItem

data class ReposModel(
    val repos: List<SearchItem>
) : Model