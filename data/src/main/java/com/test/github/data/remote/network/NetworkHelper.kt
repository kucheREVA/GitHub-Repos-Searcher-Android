package com.test.github.data.remote.network

//TODO Need to rework this class to provide all connection stuff
interface NetworkHelper {

    fun getGitHubApiService(): GitHubApiService
}