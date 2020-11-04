package com.test.github.domain.usecase

import com.test.github.domain.model.Model
import com.test.github.domain.model.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import org.koin.core.KoinComponent
import kotlin.coroutines.CoroutineContext

typealias SimpleResult = Result<Model, Throwable>

abstract class UseCase<in Params> : CoroutineScope, KoinComponent {

    private val parentJob = SupervisorJob()
    private val mainDispatcher = Dispatchers.Main
    private val backgroundDispatcher = Dispatchers.Default

    override val coroutineContext: CoroutineContext
        get() = parentJob + mainDispatcher

    abstract fun run(params: Params): Flow<SimpleResult>

    protected open suspend fun onStart(params: Params): SimpleResult? = null

    operator fun invoke(params: Params): Flow<SimpleResult> = run(params)
        .onStart { onStart(params)?.let { emit(it) } }
        .flowOn(backgroundDispatcher)
        .catch { e -> emit(Result.Failure(e)) }

}