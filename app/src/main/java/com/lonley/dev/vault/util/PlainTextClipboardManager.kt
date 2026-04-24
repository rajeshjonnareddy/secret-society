package com.lonley.dev.vault.util

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

/**
 * A ClipboardManager wrapper that strips styled text annotations (e.g., yellow
 * background highlights from Android clipboard suggestions) so pasted text
 * arrives as plain text only.
 */
class PlainTextClipboardManager(
    private val delegate: ClipboardManager
) : ClipboardManager {
    override fun getText(): AnnotatedString? {
        val text = delegate.getText() ?: return null
        return AnnotatedString(text.text)
    }

    override fun setText(annotatedString: AnnotatedString) {
        delegate.setText(AnnotatedString(annotatedString.text))
    }
}
