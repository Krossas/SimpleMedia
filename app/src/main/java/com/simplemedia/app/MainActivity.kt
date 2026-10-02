package com.simplemedia.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.simplemedia.app.service.PlaybackService
import com.simplemedia.app.ui.theme.SimpleMediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleMediaTheme {
                SimpleMediaApp()
            }
        }
    }
}

@Composable
fun SimpleMediaApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.Music) }
    val context = LocalContext.current

    val musicFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { treeUri ->
            if (treeUri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                viewModel.setMusicFolder(treeUri)
            }
        }
    )

    val videoFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { treeUri ->
            if (treeUri != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                viewModel.setVideoFolder(treeUri)
            }
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                AppTab.Music -> MusicScreen(
                    folderUri = uiState.musicFolderUri,
                    currentTrackUri = uiState.currentAudioUri,
                    currentTrackTitle = uiState.currentAudioTitle,
                    queue = uiState.playbackQueue,
                    onChooseFolder = { musicFolderPicker.launch(null) },
                    onClearFolder = { viewModel.clearMusicFolder() },
                    onSelectAudio = { uri, title ->
                        viewModel.setPlaybackQueue(listOf(com.simplemedia.app.queue.QueuedMediaItem(uri, title, uiState.musicFolderUri, com.simplemedia.app.queue.MediaKind.Audio)))
                    },
                    onPlayFolder = { items -> viewModel.setPlaybackQueue(items) },
                    onClearAudio = { viewModel.clearCurrentAudio() },
                    onNext = { viewModel.playNextInQueue() },
                    onPrevious = { viewModel.playPreviousInQueue() }
                )
                AppTab.Video -> VideoScreen(
                    folderUri = uiState.videoFolderUri,
                    onChooseFolder = { videoFolderPicker.launch(null) },
                    onClearFolder = { viewModel.clearVideoFolder() }
                )
                AppTab.Settings -> SettingsScreen(
                    musicFolderUri = uiState.musicFolderUri,
                    videoFolderUri = uiState.videoFolderUri,
                    onChooseMusicFolder = { musicFolderPicker.launch(null) },
                    onChooseVideoFolder = { videoFolderPicker.launch(null) },
                    onClearMusicFolder = { viewModel.clearMusicFolder() },
                    onClearVideoFolder = { viewModel.clearVideoFolder() }
                )
            }
        }
    }
}

enum class AppTab(val label: String) {
    Music("Música"),
    Video("Vídeos"),
    Settings("Ajustes")
}

@Composable
fun MusicScreen(
    folderUri: String?,
    currentTrackUri: String?,
    currentTrackTitle: String?,
    queue: List<com.simplemedia.app.queue.QueuedMediaItem>,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit,
    onSelectAudio: (uri: String, title: String) -> Unit,
    onPlayFolder: (List<com.simplemedia.app.queue.QueuedMediaItem>) -> Unit,
    onClearAudio: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(currentTrackUri) {
        if (currentTrackUri == null) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            isPlaying = false
            playbackError = null
            context.stopService(Intent(context, PlaybackService::class.java))
        } else {
            try {
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(currentTrackUri)))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                playbackError = null
                PlaybackService.start(context, currentTrackUri, currentTrackTitle ?: "SimpleMedia")
            } catch (exception: Exception) {
                Log.e("SimpleMedia", "Failed to open audio track: $currentTrackUri", exception)
                playbackError = "No se pudo reproducir este archivo de audio. Se continuará con la cola si existe."
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                isPlaying = false
                onClearAudio()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FolderScreen(
            title = "Música",
            folderUri = folderUri,
            onChooseFolder = onChooseFolder,
            onClearFolder = onClearFolder,
            extensionFilter = { it.matchesMediaExtension() },
            onSelectMediaFile = { uri, title -> onSelectAudio(uri, title) },
            onPlayFolder = { items -> onPlayFolder(items) }
        )

        if (playbackError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = playbackError ?: "Error de reproducción",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (currentTrackUri != null) {
            AudioPlayerCard(
                title = currentTrackTitle ?: "Reproduciendo",
                isPlaying = isPlaying,
                canGoPrevious = queue.isNotEmpty() && queue.size > 1,
                canGoNext = queue.isNotEmpty() && queue.size > 1,
                onTogglePlayback = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        PlaybackService.pause(context)
                    } else {
                        exoPlayer.play()
                        PlaybackService.play(context)
                    }
                },
                onStop = {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    PlaybackService.stop(context)
                    onClearAudio()
                },
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
    }
}

@Composable
fun AudioPlayerCard(
    title: String,
    isPlaying: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onTogglePlayback: () -> Unit,
    onStop: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Reproduciendo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onPrevious, enabled = canGoPrevious) {
                    Text("Anterior")
                }
                Button(onClick = onTogglePlayback) {
                    Text(if (isPlaying) "Pausar" else "Reproducir")
                }
                Button(onClick = onNext, enabled = canGoNext) {
                    Text("Siguiente")
                }
                Button(onClick = onStop) {
                    Text("Parar")
                }
            }
        }
    }
}

@Composable
fun VideoScreen(
    folderUri: String?,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit
) {
    val context = LocalContext.current
    var selectedVideoUri by remember { mutableStateOf<String?>(null) }
    var videoError by remember { mutableStateOf<String?>(null) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(selectedVideoUri) {
        if (selectedVideoUri == null) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            videoError = null
            return@LaunchedEffect
        }

        try {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(selectedVideoUri)))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            videoError = null
        } catch (exception: Exception) {
            Log.e("SimpleMedia", "Failed to open video track: $selectedVideoUri", exception)
            videoError = "No se pudo reproducir este archivo de vídeo. Puede probar con otro archivo."
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            selectedVideoUri = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        FolderScreen(
            title = "Vídeos",
            folderUri = folderUri,
            onChooseFolder = onChooseFolder,
            onClearFolder = onClearFolder,
            extensionFilter = { it.matchesVideoExtension() },
            onSelectMediaFile = { uri, _ -> selectedVideoUri = uri }
        )

        if (videoError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = videoError ?: "Error de reproducción",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (selectedVideoUri != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Reproducción de vídeo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                controllerAutoShow = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                            selectedVideoUri = null
                        }
                    ) {
                        Text("Cerrar vídeo")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    musicFolderUri: String?,
    videoFolderUri: String?,
    onChooseMusicFolder: () -> Unit,
    onChooseVideoFolder: () -> Unit,
    onClearMusicFolder: () -> Unit,
    onClearVideoFolder: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ajustes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        SettingsFolderCard(
            title = "Carpeta de música",
            folderUri = musicFolderUri,
            onChoose = onChooseMusicFolder,
            onClear = onClearMusicFolder
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingsFolderCard(
            title = "Carpeta de vídeo",
            folderUri = videoFolderUri,
            onChoose = onChooseVideoFolder,
            onClear = onClearVideoFolder
        )
        TextButton(
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.flaticon.com/free-icons/video-player")
                    )
                )
            }
        ) {
            Text("Icono: Video player icons creados por smashingstocks - Flaticon")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsFolderCard(
    title: String,
    folderUri: String?,
    onChoose: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onChoose
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = folderUri?.let { "Configurada: $it" } ?: "No configurada",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onClear) {
                    Text("Limpiar")
                }
                Spacer(modifier = Modifier.height(0.dp))
                Button(onClick = onChoose) {
                    Text("Seleccionar")
                }
            }
        }
    }
}

@Composable
fun FolderScreen(
    title: String,
    folderUri: String?,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit,
    extensionFilter: (String) -> Boolean,
    onSelectMediaFile: ((uri: String, title: String) -> Unit)? = null,
    onPlayFolder: ((List<com.simplemedia.app.queue.QueuedMediaItem>) -> Unit)? = null
) {
    val context = LocalContext.current
    var currentFolderUri by remember(folderUri) { mutableStateOf(folderUri) }

    val root = currentFolderUri?.let { Uri.parse(it) }?.let { DocumentFile.fromTreeUri(context, it) }
    val entries = root?.listFiles()
        ?.filter { it.name != null }
        ?.map { file ->
            FolderEntry(
                name = file.name ?: "",
                isDirectory = file.isDirectory,
                uri = file.uri.toString()
            )
        }
        ?.filter { entry ->
            entry.isDirectory || extensionFilter(entry.name)
        }
        ?.sortedWith { left, right ->
            when {
                left.isDirectory && !right.isDirectory -> -1
                !left.isDirectory && right.isDirectory -> 1
                else -> com.simplemedia.app.util.NaturalSort.sort(listOf(left.name, right.name)).let {
                    if (it.first() == left.name) -1 else 1
                }
            }
        }
        ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onChooseFolder) {
                Text("Seleccionar carpeta")
            }
            if (folderUri != null) {
                Button(onClick = onClearFolder) {
                    Text("Limpiar")
                }
            }
            if (folderUri != null && onPlayFolder != null) {
                Button(onClick = {
                    val queue = root
                        ?.listFiles()
                        ?.filter { it.name != null }
                        ?.filter { !it.isDirectory }
                        ?.filter { extensionFilter(it.name ?: "") }
                        ?.map { file ->
                            com.simplemedia.app.queue.QueuedMediaItem(
                                uri = file.uri.toString(),
                                title = file.name ?: "",
                                sourceFolderUri = folderUri,
                                kind = com.simplemedia.app.queue.MediaKind.Audio
                            )
                        }
                        ?.filter { it.title.isNotBlank() }
                        ?.let { com.simplemedia.app.queue.MediaQueuePlanner.buildQueue(it) }
                        ?: emptyList()

                    if (queue.isNotEmpty()) {
                        onPlayFolder(queue)
                    }
                }) {
                    Text("Reproducir carpeta")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (folderUri == null) {
            Text(
                text = "Todavía no has configurado una carpeta para $title.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "Ruta: ${currentFolderUri ?: folderUri}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (entries.isEmpty()) {
                Text(
                    text = "No se encontraron elementos compatibles en esta carpeta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn {
                    items(entries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                if (entry.isDirectory) {
                                    currentFolderUri = entry.uri
                                } else {
                                    onSelectMediaFile?.invoke(entry.uri, entry.name)
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (entry.isDirectory) FontWeight.SemiBold else FontWeight.Normal
                                )
                                if (entry.isDirectory) {
                                    Text(
                                        text = "Carpeta",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "Archivo compatible",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class FolderEntry(
    val name: String,
    val isDirectory: Boolean,
    val uri: String
)

private fun String.matchesMediaExtension(): Boolean {
    val normalized = lowercase()
    return normalized.endsWith(".mp3") ||
        normalized.endsWith(".flac") ||
        normalized.endsWith(".aac") ||
        normalized.endsWith(".m4a") ||
        normalized.endsWith(".wav") ||
        normalized.endsWith(".ogg") ||
        normalized.endsWith(".opus") ||
        normalized.endsWith(".aiff") ||
        normalized.endsWith(".wma")
}

private fun String.matchesVideoExtension(): Boolean {
    val normalized = lowercase()
    return normalized.endsWith(".mp4") ||
        normalized.endsWith(".mkv") ||
        normalized.endsWith(".avi") ||
        normalized.endsWith(".webm") ||
        normalized.endsWith(".mov") ||
        normalized.endsWith(".m4v") ||
        normalized.endsWith(".ts")
}
