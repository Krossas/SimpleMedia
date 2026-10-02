package com.simplemedia.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simplemedia.app.data.FolderSettingsStore
import com.simplemedia.app.queue.QueuedMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUiState(
    val musicFolderUri: String? = null,
    val videoFolderUri: String? = null,
    val currentAudioUri: String? = null,
    val currentAudioTitle: String? = null,
    val playbackQueue: List<QueuedMediaItem> = emptyList(),
    val currentQueueIndex: Int = -1
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val folderSettingsStore = FolderSettingsStore(application.applicationContext)

    private val _uiState = MutableStateFlow(
        AppUiState(
            musicFolderUri = folderSettingsStore.getMusicFolderUri(),
            videoFolderUri = folderSettingsStore.getVideoFolderUri(),
            currentAudioUri = folderSettingsStore.getLastAudioUri(),
            currentAudioTitle = folderSettingsStore.getLastAudioTitle(),
            playbackQueue = folderSettingsStore.getPlaybackQueue(),
            currentQueueIndex = folderSettingsStore.getCurrentQueueIndex()
        )
    )
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun setMusicFolder(uri: Uri?) {
        viewModelScope.launch {
            folderSettingsStore.saveMusicFolderUri(uri)
            _uiState.value = _uiState.value.copy(musicFolderUri = uri?.toString())
        }
    }

    fun setVideoFolder(uri: Uri?) {
        viewModelScope.launch {
            folderSettingsStore.saveVideoFolderUri(uri)
            _uiState.value = _uiState.value.copy(videoFolderUri = uri?.toString())
        }
    }

    fun clearMusicFolder() {
        viewModelScope.launch {
            folderSettingsStore.saveMusicFolderUri(null)
            _uiState.value = _uiState.value.copy(musicFolderUri = null)
        }
    }

    fun clearVideoFolder() {
        viewModelScope.launch {
            folderSettingsStore.saveVideoFolderUri(null)
            _uiState.value = _uiState.value.copy(videoFolderUri = null)
        }
    }

    fun setCurrentAudio(uri: String?, title: String?) {
        folderSettingsStore.saveCurrentAudio(uri, title)
        _uiState.value = _uiState.value.copy(
            currentAudioUri = uri,
            currentAudioTitle = title
        )
    }

    fun clearCurrentAudio() {
        folderSettingsStore.saveCurrentAudio(null, null)
        folderSettingsStore.savePlaybackQueue(_uiState.value.playbackQueue, -1)
        _uiState.value = _uiState.value.copy(
            currentAudioUri = null,
            currentAudioTitle = null,
            currentQueueIndex = -1
        )
    }

    fun setPlaybackQueue(items: List<QueuedMediaItem>, startIndex: Int = 0) {
        val safeStartIndex = if (items.isEmpty()) -1 else startIndex.coerceIn(0, items.lastIndex)
        _uiState.value = _uiState.value.copy(
            playbackQueue = items,
            currentQueueIndex = safeStartIndex
        )
        if (items.isNotEmpty()) {
            val chosen = items[safeStartIndex]
            folderSettingsStore.saveCurrentAudio(chosen.uri, chosen.title)
            folderSettingsStore.savePlaybackQueue(items, safeStartIndex)
            _uiState.value = _uiState.value.copy(
                currentAudioUri = chosen.uri,
                currentAudioTitle = chosen.title
            )
        } else {
            folderSettingsStore.saveCurrentAudio(null, null)
            folderSettingsStore.savePlaybackQueue(emptyList(), -1)
        }
    }

    fun appendToQueue(items: List<QueuedMediaItem>) {
        if (items.isEmpty()) return
        val nextQueue = _uiState.value.playbackQueue + items
        folderSettingsStore.savePlaybackQueue(nextQueue, _uiState.value.currentQueueIndex)
        _uiState.value = _uiState.value.copy(playbackQueue = nextQueue)
    }

    fun playNextInQueue() {
        val currentIndex = _uiState.value.currentQueueIndex
        if (currentIndex < 0 || currentIndex >= _uiState.value.playbackQueue.lastIndex) return
        val nextItem = _uiState.value.playbackQueue[currentIndex + 1]
        val nextIndex = currentIndex + 1
        folderSettingsStore.saveCurrentAudio(nextItem.uri, nextItem.title)
        folderSettingsStore.savePlaybackQueue(_uiState.value.playbackQueue, nextIndex)
        _uiState.value = _uiState.value.copy(
            currentAudioUri = nextItem.uri,
            currentAudioTitle = nextItem.title,
            currentQueueIndex = nextIndex
        )
    }

    fun playPreviousInQueue() {
        val currentIndex = _uiState.value.currentQueueIndex
        if (currentIndex <= 0) return
        val previousItem = _uiState.value.playbackQueue[currentIndex - 1]
        val previousIndex = currentIndex - 1
        folderSettingsStore.saveCurrentAudio(previousItem.uri, previousItem.title)
        folderSettingsStore.savePlaybackQueue(_uiState.value.playbackQueue, previousIndex)
        _uiState.value = _uiState.value.copy(
            currentAudioUri = previousItem.uri,
            currentAudioTitle = previousItem.title,
            currentQueueIndex = previousIndex
        )
    }

    fun clearPlaybackQueue() {
        folderSettingsStore.saveCurrentAudio(null, null)
        folderSettingsStore.savePlaybackQueue(emptyList(), -1)
        _uiState.value = _uiState.value.copy(
            playbackQueue = emptyList(),
            currentQueueIndex = -1
        )
    }
}
