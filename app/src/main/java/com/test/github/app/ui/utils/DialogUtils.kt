package com.test.github.app.ui.utils

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.github.app.R
import timber.log.Timber

fun MaterialAlertDialogBuilder.showErrorDialog(throwable: Throwable? = null) {
    throwable?.run { Timber.e(this) }
    this.setTitle(context.getString(R.string.dialog_error_title))
        .setMessage(throwable?.message ?: context.getString(R.string.dialog_error_messge))
        .setPositiveButton(context.getString(R.string.dialog_btn_ok), null)
        .show()
}