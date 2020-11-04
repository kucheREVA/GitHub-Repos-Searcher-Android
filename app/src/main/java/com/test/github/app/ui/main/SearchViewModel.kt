package com.test.github.app.ui.main

import android.os.CountDownTimer
import androidx.lifecycle.*
import com.test.github.domain.item.CooldownItem
import com.test.github.domain.item.RepoItem
import com.test.github.domain.item.SearchItem
import com.test.github.domain.model.ReposModel
import com.test.github.domain.model.Result
import com.test.github.domain.model.SearchQuery
import com.test.github.domain.usecase.SimpleResult
import com.test.github.domain.usecase.UseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SearchViewModel(
    getReposUseCase: UseCase<Unit>,
    private val searchRepositoryUseCase: UseCase<SearchQuery>,
    private val updateHistoryUseCase: UseCase<RepoItem>,
    private val clearReposUseCase: UseCase<Unit>
) : ViewModel() {

    private val loadNextPageLiveData = MutableLiveData<SimpleResult>()
    private val reposLiveData = getReposUseCase.invoke(Unit).asLiveData()
    private val currentPageLiveData = MutableLiveData(0)
    private val countDownLiveData = MutableLiveData(0L)

    private val timer: CountDownTimer = object : CountDownTimer(60_000, 1_000) {

        override fun onTick(p0: Long) {
            countDownLiveData.value = p0
        }

        override fun onFinish() {
            countDownLiveData.value = 0
            eventLiveData.value = Result.State.COOL_DOWN_RELEASED
        }
    }

    val eventLiveData = MediatorLiveData<SimpleResult>().apply {

        var _result: SimpleResult? = null
        var coolDownValue: Long = 0

        fun update() {
            val result = _result ?: return

            if (coolDownValue > 0 && result is Result.Success) {
                val model = result.successData as ReposModel
                val repos = mutableListOf<SearchItem>()
                repos.addAll(model.repos)
                repos.add(CooldownItem(coolDownValue = (coolDownValue / 1_000).toString()))
                postValue(Result.Success(model.copy(repos = repos)))
            } else {
                postValue(result)
            }
        }

        fun update(result: SimpleResult) {
            postValue(result)
        }

        addSource(loadNextPageLiveData) {
            update(it)
        }

        addSource(reposLiveData) {
            _result = it
            update()
        }

        addSource(countDownLiveData) {
            coolDownValue = it
            update()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    fun clearRepos() {
        currentPageLiveData.value = 0
        viewModelScope.launch {
            clearReposUseCase.invoke(Unit).collect()
        }
    }


    fun offerNewRepos(searchString: String) {
        viewModelScope.launch {
            currentPageLiveData.value?.let { page ->
                if (countDownLiveData.value == 0L) {
                    searchRepositoryUseCase.invoke(SearchQuery(searchString, page))
                        .collect { loadNextPageLiveData.postValue(it) }
                }
            }
        }
    }

    fun countDownDelay() {
        timer.start()
    }

    fun updateHistory(item: RepoItem) = viewModelScope.launch {
        updateHistoryUseCase.invoke(item).collect()
    }

    fun updateCurrentPage(page: Int) {
        currentPageLiveData.value = page
    }
}