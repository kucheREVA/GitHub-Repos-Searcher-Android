package com.test.github.app.ui.main

import androidx.lifecycle.*
import com.test.github.domain.item.RepoItem
import com.test.github.domain.model.SearchQuery
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainViewModel(
    getReposUseCase: UseCase<Unit>,
    private val searchRepositoryUseCase: UseCase<SearchQuery>,
    private val updateHistoryUseCase: UseCase<RepoItem>,
    private val clearReposUseCase: UseCase<Unit>
) : ViewModel() {

    private val loadNextPageLiveData = MutableLiveData<SimpleResult>()
    private val reposLiveData = getReposUseCase.invoke(Unit).asLiveData()

    val eventLiveData = MediatorLiveData<SimpleResult>().apply {

        fun update(result: SimpleResult) {
            postValue(result)
        }

        addSource(loadNextPageLiveData) {
            update(it)
        }

        addSource(reposLiveData) {
            update(it)
        }
    }

    suspend fun clearRepos() = clearReposUseCase.invoke(Unit).collect()

    suspend fun offerNewRepos(searchString: String, page: Int) =
        searchRepositoryUseCase.invoke(SearchQuery(searchString, page))
            .collect { loadNextPageLiveData.postValue(it) }


    fun updateHistory(item: RepoItem) = viewModelScope.launch {
        updateHistoryUseCase.invoke(item).collect()
    }
}