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

    fun performLongPress(view: View, enabled: Boolean) {
        if (enabled) {
            view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    fun performReject(view: View, enabled: Boolean) {
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                view.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
        }
    }
}
