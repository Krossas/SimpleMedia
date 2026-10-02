package com.simplemedia.app.queue

data class QueuedMediaItem(
    val uri: String,
    val title: String,
    val sourceFolderUri: String? = null,
    val kind: MediaKind = MediaKind.Audio
)

enum class MediaKind {
    Audio,
    Video
}
