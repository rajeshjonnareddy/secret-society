package com.lonley.dev.vault.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {
    fun performClick(view: View, enabled: Boolean) {
        if (enabled) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CONTEXT_CLICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    fun performScrollTick(view: View, enabled: Boolean) {
        if (enabled) {
            view.performHapticFeedback(
                HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }
}
