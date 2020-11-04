package com.test.github.data.remote.network

import com.test.github.domain.repository.AccountRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class DefaultNetworkHelper(
    private val defaultUrl: String,
    private val accountRepository: AccountRepository
) : NetworkHelper {

    private lateinit var httpClient: OkHttpClient.Builder

    private lateinit var retrofit: Retrofit.Builder

    private val loggerInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val authenticationInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()
            return accountRepository.getAuthToken()?.let { token ->
                val request = requestBuilder
                    .addHeader("Authorization", token)
                    .build()
                chain.proceed(request)
            } ?: chain.proceed(requestBuilder.build())
        }
    }

    init {
        val trustManagers = prepareTrustManager()
        val sslSocketFactory = prepareSslSocketFactory(trustManagers)

        sslSocketFactory?.let {
            httpClient = prepareOkHttpClient(sslSocketFactory, trustManagers)
                .addInterceptor(loggerInterceptor)
                .addInterceptor(authenticationInterceptor)

            retrofit = prepareRetrofit().client(httpClient.build())
        } ?: throw Exception("SSL Not initialized!")
    }

    override fun getGitHubApiService(): GitHubApiService {
        return retrofit.build().create(GitHubApiService::class.java)
    }

    private fun prepareRetrofit() = Retrofit.Builder()
        .baseUrl(defaultUrl)
        .addConverterFactory(GsonConverterFactory.create())

    private fun prepareOkHttpClient(
        sslSocketFactory: SSLSocketFactory,
        trustManagers: Array<TrustManager>
    ) = OkHttpClient().newBuilder()
        .sslSocketFactory(sslSocketFactory, trustManagers[0] as X509TrustManager)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

    private fun prepareSslSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory? {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManagers, SecureRandom())
            return sslContext.socketFactory
        } catch (e: NoSuchAlgorithmException) {
            Timber.e(e)
        } catch (e: KeyManagementException) {
            Timber.e(e)
        }
        return null
    }

    private fun prepareTrustManager(): Array<TrustManager> {
        try {
            val algorithm = TrustManagerFactory.getDefaultAlgorithm()
            val factory = TrustManagerFactory.getInstance(algorithm)
            factory.init(null as KeyStore?)
            return factory.trustManagers
        } catch (e: Exception) {
            Timber.e(e)
        }
        return emptyArray()
    }
}