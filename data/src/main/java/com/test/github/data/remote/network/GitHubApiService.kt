package com.test.github.data.remote.network

import com.test.github.domain.data.SearchData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GitHubApiService {

    @GET("/search/repositories")
    suspend fun searchRepos(
        @Header("Authorization") oauthToken: String,
        @Query("page") page: Int,
        @Query("q") query: String,
        @Query("sort") sort: String,
        @Query("order") order: String,
        @Query("per_page") size: Int = 15
    ): Response<SearchData>
}