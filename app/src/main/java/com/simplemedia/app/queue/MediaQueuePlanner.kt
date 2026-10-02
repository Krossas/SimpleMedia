package com.simplemedia.app.queue

import com.simplemedia.app.util.NaturalSort

object MediaQueuePlanner {
    fun buildQueue(items: List<QueuedMediaItem>): List<QueuedMediaItem> {
        return items
            .asSequence()
            .filter { it.uri.isNotBlank() && it.title.isNotBlank() }
            .distinctBy { it.uri }
            .sortedWith { left, right ->
                val ordered = NaturalSort.sort(listOf(left.title, right.title))
                when {
                    ordered.first() == left.title -> -1
                    ordered.first() == right.title -> 1
                    else -> 0
                }
            }
            .toList()
    }
}
