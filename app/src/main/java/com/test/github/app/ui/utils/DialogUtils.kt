package com.test.github.app.ui.utils

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.github.app.R
import timber.log.Timber

fun MaterialAlertDialogBuilder.showErrorDialog(message: String? = null) {
    this.setTitle(context.getString(R.string.dialog_error_title))
        .setMessage(message ?: context.getString(R.string.dialog_error_messge))
        .setPositiveButton(context.getString(R.string.dialog_btn_ok), null)
        .setCancelable(false)
        .show()
}

fun MaterialAlertDialogBuilder.showMessageDialog(title: String, message: String) {
    this.setTitle(title)
        .setMessage(message)
        .setPositiveButton(context.getString(R.string.dialog_btn_ok), null)
        .setCancelable(false)
        .show()
}