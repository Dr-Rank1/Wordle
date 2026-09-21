package com.rank.lexi.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Safely unwrap the context hierarchy to find the enclosing [Activity],
 * avoiding ClassCastExceptions when Compose runs inside wrapped contexts.
 */
fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
