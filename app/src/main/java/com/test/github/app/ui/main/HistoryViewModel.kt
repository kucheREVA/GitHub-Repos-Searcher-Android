package com.test.github.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.test.github.domain.usecase.UseCase

class HistoryViewModel(
    getHistoryUseCase: UseCase<Unit>
) : ViewModel() {

    val historyLiveData = getHistoryUseCase.invoke(Unit).asLiveData()
}