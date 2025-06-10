package com.notifyu.app.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

/**
 * Hides the soft keyboard from the screen.
 *
 * This utility function is helpful when the keyboard remains open after a user performs
 * an action (like logging in, navigating to another screen, or clicking a button),
 * which can lead to a visually jarring or awkward experience.
 *
 * ✅ Recommended usage:
 * Call this when navigating away from a screen or after a user action where the keyboard
 * should no longer be visible, to ensure a smooth and clean user experience.
 *
 * @param context The context from which to retrieve the current focused view and input method manager.
 */
fun hideKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = (context as? Activity)?.currentFocus
    currentFocus?.let {
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}
