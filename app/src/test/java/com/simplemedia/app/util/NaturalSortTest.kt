package com.simplemedia.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NaturalSortTest {
    @Test
    fun `sorts episode names with natural numbering`() {
        val items = listOf(
            "Capitulo10",
            "Capitulo1",
            "Capitulo2",
            "S01E10",
            "S01E02",
            "S01E01",
            "Capitulo3"
        )

        val expected = listOf(
            "Capitulo1",
            "Capitulo2",
            "Capitulo3",
            "Capitulo10",
            "S01E01",
            "S01E02",
            "S01E10"
        )

        assertEquals(expected, NaturalSort.sort(items))
    }

    @Test
    fun `sorts unicode and numeric names consistently`() {
        val items = listOf(
            "Álbum clásico — 2026.flac",
            "José González - Canción nº 1.mp3",
            "Capítulo 10 - Intro.mkv",
            "Capítulo 2 - Intro.mkv"
        )

        val expected = listOf(
            "Capítulo 2 - Intro.mkv",
            "Capítulo 10 - Intro.mkv",
            "José González - Canción nº 1.mp3",
            "Álbum clásico — 2026.flac"
        )

        assertEquals(expected, NaturalSort.sort(items))
    }
}
