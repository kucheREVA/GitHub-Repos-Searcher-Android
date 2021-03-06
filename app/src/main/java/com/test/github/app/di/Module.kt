package com.test.github.app.di

import android.accounts.AbstractAccountAuthenticator
import android.accounts.AccountManager
import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.test.github.app.BuildConfig
import com.test.github.app.ui.login.LoginViewModel
import com.test.github.app.ui.login.SplashViewModel
import com.test.github.app.ui.main.HistoryViewModel
import com.test.github.app.ui.main.SearchViewModel
import com.test.github.data.account.AccountAuthenticator
import com.test.github.data.database.AppDatabase
import com.test.github.data.remote.network.DefaultNetworkHelper
import com.test.github.data.remote.network.NetworkHelper
import com.test.github.data.repository.DefaultAccountRepository
import com.test.github.data.repository.DefaultGitHubRepository
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.AccountModel
import com.test.github.domain.model.SearchQuery
import com.test.github.domain.repository.AccountRepository
import com.test.github.domain.repository.GitHubRepository
import com.test.github.domain.usecase.UseCase
import com.test.github.domain.usecase.account.CreateAccountUseCase
import com.test.github.domain.usecase.account.GetAccountUseCase
import com.test.github.domain.usecase.history.GetHistoryUseCase
import com.test.github.domain.usecase.history.UpdateHistoryUseCase
import com.test.github.domain.usecase.repos.ClearReposUseCase
import com.test.github.domain.usecase.repos.GetReposUseCase
import com.test.github.domain.usecase.search.SearchReposUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModules = module {
    viewModel { LoginViewModel(get(named(CreateAccountUseCase::class.java.simpleName))) }
    viewModel { HistoryViewModel(get(named(GetHistoryUseCase::class.java.simpleName))) }
    viewModel {
        SplashViewModel(
            get(named(GetAccountUseCase::class.java.simpleName)),
            get(named(ClearReposUseCase::class.java.simpleName))
        )
    }
    viewModel {
        SearchViewModel(
            get(named(GetReposUseCase::class.java.simpleName)),
            get(named(SearchReposUseCase::class.java.simpleName)),
            get(named(UpdateHistoryUseCase::class.java.simpleName)),
            get(named(ClearReposUseCase::class.java.simpleName))
        )
    }
}

val repositoryModule = module {
    single<AccountRepository> { DefaultAccountRepository(get()) }
    single<GitHubRepository> { DefaultGitHubRepository(get(), get()) }
}

val useCaseModule = module {

    factory<UseCase<SearchQuery>>(named(SearchReposUseCase::class.java.simpleName)) {
        SearchReposUseCase(get())
    }

    factory<UseCase<Unit>>(named(GetAccountUseCase::class.java.simpleName)) {
        GetAccountUseCase(get())
    }

    factory<UseCase<AccountModel>>(named(CreateAccountUseCase::class.java.simpleName)) {
        CreateAccountUseCase(get())
    }

    factory<UseCase<Unit>>(named(GetReposUseCase::class.java.simpleName)) {
        GetReposUseCase(get())
    }

    factory<UseCase<Unit>>(named(ClearReposUseCase::class.java.simpleName)) {
        ClearReposUseCase(get())
    }

    factory<UseCase<RepoItem>>(named(UpdateHistoryUseCase::class.java.simpleName)) {
        UpdateHistoryUseCase(get())
    }

    factory<UseCase<Unit>>(named(GetHistoryUseCase::class.java.simpleName)) { GetHistoryUseCase(get()) }
}

val networkModule = module {

    fun provideApiService(networkHelper: NetworkHelper) = networkHelper.getGitHubApiService()

    single<NetworkHelper> { DefaultNetworkHelper(BuildConfig.BASE_URL, get()) }

    single { provideApiService(get()) }
}

val dbModule = module {

    fun provideAppDatabase(context: Context) = Room
        .databaseBuilder(context, AppDatabase::class.java, "repos-db")
        .build()

    single { provideAppDatabase(get()) }
}

val systemServicesModule = module {
    factory<AbstractAccountAuthenticator> { AccountAuthenticator(get()) }
    factory<AccountManager> { AccountManager.get(get()) }

    single { FirebaseAuth(get()) }
}

val appModules = listOf(
    systemServicesModule,
    dbModule,
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModules
)