package com.baidu.duer.files.util

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.baidu.duer.files.R

fun <I> ActivityResultLauncher<I>.launchSafe(input: I, context: Context) {
    try {
        launch(input)
    } catch (e: ActivityNotFoundException) {
        context.showToast(R.string.activity_not_found)
    }
}

fun <I> ActivityResultLauncher<I>.launchSafe(input: I, fragment: Fragment) {
    try {
        launch(input)
    } catch (e: ActivityNotFoundException) {
        fragment.showToast(R.string.activity_not_found)
    }
}

fun <I> ActivityResultLauncher<I>.launchSafe(
    input: I,
    options: ActivityOptionsCompat?,
    context: Context
) {
    try {
        launch(input, options)
    } catch (e: ActivityNotFoundException) {
        context.showToast(R.string.activity_not_found)
    }
}

fun <I> ActivityResultLauncher<I>.launchSafe(
    input: I,
    options: ActivityOptionsCompat?,
    fragment: Fragment
) {
    try {
        launch(input, options)
    } catch (e: ActivityNotFoundException) {
        fragment.showToast(R.string.activity_not_found)
    }
}
