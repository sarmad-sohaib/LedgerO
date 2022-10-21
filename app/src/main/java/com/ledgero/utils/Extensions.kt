package com.ledgero.utils

import android.content.BroadcastReceiver
import android.view.View
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DateFormat
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob()).launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}