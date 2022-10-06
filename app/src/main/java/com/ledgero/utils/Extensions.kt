package com.ledgero.utils

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat

fun Long.toDateTimeFormat(): String {
    return DateFormat.getDateTimeInstance().format(this)
}

fun View.showSnackBar(string: String) {
    Snackbar.make(
        this,
        string,
        Snackbar.LENGTH_LONG,
    ).show()
}