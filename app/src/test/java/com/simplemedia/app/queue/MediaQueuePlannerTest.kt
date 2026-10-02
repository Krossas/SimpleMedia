package com.simplemedia.app.queue

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaQueuePlannerTest {
    @Test
    fun `buildQueue keeps natural order for folder media files`() {
        val entries = listOf(
            QueuedMediaItem("content://disk/Capitulo10.mkv", "Capitulo10.mkv"),
            QueuedMediaItem("content://disk/Capitulo1.mkv", "Capitulo1.mkv"),
            QueuedMediaItem("content://disk/Capitulo2.mkv", "Capitulo2.mkv"),
            QueuedMediaItem("content://disk/Capitulo3.mkv", "Capitulo3.mkv")
        )

        val ordered = MediaQueuePlanner.buildQueue(entries)

        assertEquals(
            listOf(
                "Capitulo1.mkv",
                "Capitulo2.mkv",
                "Capitulo3.mkv",
                "Capitulo10.mkv"
            ),
            ordered.map { it.title }
        )
    }

    @Test
    fun `buildQueue ignores blank or invalid entries`() {
        val entries = listOf(
            QueuedMediaItem("content://disk/Capitulo2.mkv", "Capitulo2.mkv"),
            QueuedMediaItem("", ""),
            QueuedMediaItem("content://disk/Capitulo1.mkv", "Capitulo1.mkv"),
            QueuedMediaItem("content://disk/", " ")
        )

        val ordered = MediaQueuePlanner.buildQueue(entries)

        assertEquals(
            listOf(
                "Capitulo1.mkv",
                "Capitulo2.mkv"
            ),
            ordered.map { it.title }
        )
    }
}
