package com.test.github.app.ui.utils

import android.text.TextWatcher
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun TextInputEditText.textInputAsFlow() = callbackFlow {
    val watcher: TextWatcher = doOnTextChanged { text, _, _, _ -> offer(text) }

    awaitClose { this@textInputAsFlow.removeTextChangedListener(watcher) }
}