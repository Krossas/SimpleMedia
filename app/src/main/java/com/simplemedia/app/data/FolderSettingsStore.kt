package com.simplemedia.app.data

import android.content.Context
import android.net.Uri
import com.simplemedia.app.queue.MediaKind
import com.simplemedia.app.queue.QueuedMediaItem
import org.json.JSONArray
import org.json.JSONObject

class FolderSettingsStore(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("folder_settings", Context.MODE_PRIVATE)

    fun getMusicFolderUri(): String? = sharedPreferences.getString(KEY_MUSIC_FOLDER, null)

    fun getVideoFolderUri(): String? = sharedPreferences.getString(KEY_VIDEO_FOLDER, null)

    fun getLastAudioUri(): String? = sharedPreferences.getString(KEY_LAST_AUDIO_URI, null)

    fun getLastAudioTitle(): String? = sharedPreferences.getString(KEY_LAST_AUDIO_TITLE, null)

    fun getPlaybackQueue(): List<QueuedMediaItem> {
        val rawQueue = sharedPreferences.getString(KEY_QUEUE, null) ?: return emptyList()
        return try {
            val items = JSONArray(rawQueue)
            buildList {
                for (index in 0 until items.length()) {
                    val item = items.getJSONObject(index)
                    val sourceFolderUri = item.optString("sourceFolderUri", "")
                    add(
                        QueuedMediaItem(
                            uri = item.getString("uri"),
                            title = item.getString("title"),
                            sourceFolderUri = sourceFolderUri.ifBlank { null },
                            kind = MediaKind.valueOf(item.getString("kind"))
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getCurrentQueueIndex(): Int = sharedPreferences.getInt(KEY_CURRENT_QUEUE_INDEX, -1)

    fun saveMusicFolderUri(uri: Uri?) {
        val editor = sharedPreferences.edit()
        if (uri == null) {
            editor.remove(KEY_MUSIC_FOLDER)
        } else {
            editor.putString(KEY_MUSIC_FOLDER, uri.toString())
        }
        editor.apply()
    }

    fun saveVideoFolderUri(uri: Uri?) {
        val editor = sharedPreferences.edit()
        if (uri == null) {
            editor.remove(KEY_VIDEO_FOLDER)
        } else {
            editor.putString(KEY_VIDEO_FOLDER, uri.toString())
        }
        editor.apply()
    }

    fun saveCurrentAudio(uri: String?, title: String?) {
        val editor = sharedPreferences.edit()
        if (uri == null || title == null) {
            editor.remove(KEY_LAST_AUDIO_URI)
            editor.remove(KEY_LAST_AUDIO_TITLE)
        } else {
            editor.putString(KEY_LAST_AUDIO_URI, uri)
            editor.putString(KEY_LAST_AUDIO_TITLE, title)
        }
        editor.apply()
    }

    fun savePlaybackQueue(items: List<QueuedMediaItem>, currentIndex: Int) {
        val array = JSONArray()
        items.forEach { item ->
            val jsonObject = JSONObject()
            jsonObject.put("uri", item.uri)
            jsonObject.put("title", item.title)
            jsonObject.put("sourceFolderUri", item.sourceFolderUri ?: "")
            jsonObject.put("kind", item.kind.name)
            array.put(jsonObject)
        }

        sharedPreferences.edit()
            .putString(KEY_QUEUE, array.toString())
            .putInt(KEY_CURRENT_QUEUE_INDEX, currentIndex)
            .apply()
    }

    companion object {
        private const val KEY_MUSIC_FOLDER = "music_folder_uri"
        private const val KEY_VIDEO_FOLDER = "video_folder_uri"
        private const val KEY_LAST_AUDIO_URI = "last_audio_uri"
        private const val KEY_LAST_AUDIO_TITLE = "last_audio_title"
        private const val KEY_QUEUE = "playback_queue"
        private const val KEY_CURRENT_QUEUE_INDEX = "playback_queue_index"
    }
}
