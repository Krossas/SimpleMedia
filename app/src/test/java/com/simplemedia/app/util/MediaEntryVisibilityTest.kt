package com.simplemedia.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaEntryVisibilityTest {
    @Test
    fun `hides dot-prefixed files and folders`() {
        assertFalse(MediaEntryVisibility.isVisible(".hidden"))
        assertFalse(MediaEntryVisibility.isVisible(".cache"))
    }

    @Test
    fun `shows regular names and rejects missing names`() {
        assertTrue(MediaEntryVisibility.isVisible("Music"))
        assertTrue(MediaEntryVisibility.isVisible("track.mp3"))
        assertFalse(MediaEntryVisibility.isVisible(null))
        assertFalse(MediaEntryVisibility.isVisible(" "))
    }
}