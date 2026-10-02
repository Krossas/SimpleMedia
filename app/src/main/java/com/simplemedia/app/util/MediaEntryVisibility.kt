package com.simplemedia.app.util

object MediaEntryVisibility {
    fun isVisible(name: String?): Boolean {
        return !name.isNullOrBlank() && !name.startsWith(".")
    }
}