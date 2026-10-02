package com.simplemedia.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.simplemedia.app.queue.MediaKind
import com.simplemedia.app.queue.MediaQueuePlanner
import com.simplemedia.app.queue.QueuedMediaItem
import com.simplemedia.app.service.PlaybackService
import com.simplemedia.app.ui.theme.SimpleMediaTheme
import com.simplemedia.app.util.MediaEntryVisibility
import kotlinx.coroutines.delay

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
                    currentTrackUri = uiState.playbackQueue.getOrNull(uiState.currentQueueIndex)
                        ?.takeIf { it.kind == MediaKind.Audio }?.uri,
                    currentTrackTitle = uiState.playbackQueue.getOrNull(uiState.currentQueueIndex)
                        ?.takeIf { it.kind == MediaKind.Audio }?.title,
                    queue = uiState.playbackQueue,
                    currentQueueIndex = uiState.currentQueueIndex,
                    onChooseFolder = { musicFolderPicker.launch(null) },
                    onClearFolder = { viewModel.clearMusicFolder() },
                    onPlayQueue = { items, startIndex -> viewModel.setPlaybackQueue(items, startIndex) },
                    onClearAudio = { viewModel.clearCurrentAudio() },
                    onNext = { viewModel.playNextInQueue() },
                    onPrevious = { viewModel.playPreviousInQueue() }
                )
                AppTab.Video -> VideoScreen(
                    folderUri = uiState.videoFolderUri,
                    currentVideoUri = uiState.playbackQueue.getOrNull(uiState.currentQueueIndex)
                        ?.takeIf { it.kind == MediaKind.Video }?.uri,
                    currentVideoTitle = uiState.playbackQueue.getOrNull(uiState.currentQueueIndex)
                        ?.takeIf { it.kind == MediaKind.Video }?.title,
                    queue = uiState.playbackQueue,
                    currentQueueIndex = uiState.currentQueueIndex,
                    onChooseFolder = { videoFolderPicker.launch(null) },
                    onClearFolder = { viewModel.clearVideoFolder() },
                    onPlayQueue = { items, startIndex -> viewModel.setPlaybackQueue(items, startIndex) },
                    onNext = { viewModel.playNextInQueue() },
                    onPrevious = { viewModel.playPreviousInQueue() }
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
    currentQueueIndex: Int,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit,
    onPlayQueue: (List<QueuedMediaItem>, startIndex: Int) -> Unit,
    onClearAudio: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onNext()
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
            currentPositionMs = 0L
            durationMs = 0L
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

    LaunchedEffect(exoPlayer, currentTrackUri, isPlaying, isSeeking) {
        if (currentTrackUri == null) return@LaunchedEffect
        while (true) {
            durationMs = exoPlayer.duration.takeIf { it >= 0L } ?: 0L
            if (!isSeeking) currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (!isPlaying) break
            delay(400L)
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
            mediaKind = MediaKind.Audio,
            modifier = Modifier.weight(1f),
            onPlayQueue = onPlayQueue
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
                canGoPrevious = currentQueueIndex > 0,
                canGoNext = currentQueueIndex in 0 until queue.lastIndex,
                positionMs = currentPositionMs,
                durationMs = durationMs,
                onSeek = { position ->
                    exoPlayer.seekTo(position)
                    PlaybackService.seek(context, position)
                },
                onTogglePlayback = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        PlaybackService.pause(context)
                    } else {
                        exoPlayer.play()
                        PlaybackService.play(context)
                    }
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
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onTogglePlayback: () -> Unit,
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
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            PlaybackSeekBar(positionMs, durationMs, onSeek)
            PlaybackControls(
                isPlaying = isPlaying,
                canGoPrevious = canGoPrevious,
                canGoNext = canGoNext,
                onPrevious = onPrevious,
                onTogglePlayback = onTogglePlayback,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor),
            enabled = canGoPrevious,
            onClick = onPrevious
        ) { Text("Anterior", style = MaterialTheme.typography.labelMedium, maxLines = 1) }
        TextButton(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor),
            onClick = onTogglePlayback
        ) { Text(if (isPlaying) "Pausa" else "Reproducir", style = MaterialTheme.typography.labelMedium, maxLines = 1) }
        TextButton(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor),
            enabled = canGoNext,
            onClick = onNext
        ) { Text("Siguiente", style = MaterialTheme.typography.labelMedium, maxLines = 1) }
    }
}

@Composable
private fun PlaybackSeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableStateOf(0f) }
    val maximum = durationMs.coerceAtLeast(1L).toFloat()

    LaunchedEffect(positionMs, isScrubbing) {
        if (!isScrubbing) scrubPosition = positionMs.toFloat().coerceIn(0f, maximum)
    }

    Column {
        Slider(
            value = scrubPosition.coerceIn(0f, maximum),
            onValueChange = {
                isScrubbing = true
                scrubPosition = it
            },
            onValueChangeFinished = {
                onSeek(scrubPosition.toLong())
                isScrubbing = false
            },
            valueRange = 0f..maximum,
            enabled = durationMs > 0L
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatPlaybackTime(if (isScrubbing) scrubPosition.toLong() else positionMs), style = MaterialTheme.typography.labelSmall)
            Text(formatPlaybackTime(durationMs), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatPlaybackTime(timeMs: Long): String {
    val totalSeconds = timeMs.coerceAtLeast(0L) / 1000L
    return "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}

@Composable
fun VideoScreen(
    folderUri: String?,
    currentVideoUri: String?,
    currentVideoTitle: String?,
    queue: List<QueuedMediaItem>,
    currentQueueIndex: Int,
    onChooseFolder: () -> Unit,
    onClearFolder: () -> Unit,
    onPlayQueue: (List<QueuedMediaItem>, startIndex: Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current
    var videoError by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var followsDeviceOrientation by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val activity = remember(context) { context.findActivity() }
    var previousOrientation by remember { mutableStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    DisposableEffect(isFullscreen, activity) {
        if (isFullscreen && activity != null) {
            previousOrientation = activity.requestedOrientation
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (isFullscreen && activity != null) {
                WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
                activity.requestedOrientation = previousOrientation
            }
        }
    }

    LaunchedEffect(isFullscreen, followsDeviceOrientation, activity) {
        if (isFullscreen) {
            activity?.requestedOrientation = if (followsDeviceOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                isPlaying = isPlayingValue
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onNext()
                durationMs = exoPlayer.duration.takeIf { it >= 0L } ?: 0L
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                currentPositionMs = newPosition.positionMs.coerceAtLeast(0L)
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("SimpleMedia", "Video playback failed: ${error.errorCodeName}", error)
                videoError = "No se pudo reproducir este vídeo. Prueba con otro archivo."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(currentVideoUri) {
        if (currentVideoUri == null) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            videoError = null
            isPlaying = false
            currentPositionMs = 0L
            durationMs = 0L
            return@LaunchedEffect
        }

        try {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(currentVideoUri)))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            videoError = null
        } catch (exception: Exception) {
            Log.e("SimpleMedia", "Failed to open video track: $currentVideoUri", exception)
            videoError = "No se pudo reproducir este archivo de vídeo. Puede probar con otro archivo."
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    LaunchedEffect(exoPlayer, currentVideoUri, isPlaying, isSeeking) {
        if (currentVideoUri == null) return@LaunchedEffect
        while (true) {
            durationMs = exoPlayer.duration.takeIf { it >= 0L } ?: 0L
            if (!isSeeking) currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (!isPlaying) break
            delay(400L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FolderScreen(
                title = "Vídeos",
                folderUri = folderUri,
                onChooseFolder = onChooseFolder,
                onClearFolder = onClearFolder,
                extensionFilter = { it.matchesVideoExtension() },
                mediaKind = MediaKind.Video,
                modifier = Modifier.weight(1f),
                onPlayQueue = onPlayQueue
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

        if (currentVideoUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = currentVideoTitle ?: "Vídeo",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!isFullscreen) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    TextButton(onClick = {
                        followsDeviceOrientation = false
                        isFullscreen = true
                    }) {
                        Text("Pantalla completa", style = MaterialTheme.typography.labelMedium)
                    }
                    PlaybackSeekBar(currentPositionMs, durationMs) { exoPlayer.seekTo(it) }
                    PlaybackControls(
                        isPlaying = isPlaying,
                        canGoPrevious = currentQueueIndex > 0,
                        canGoNext = currentQueueIndex in 0 until queue.lastIndex,
                        onPrevious = onPrevious,
                        onTogglePlayback = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        onNext = onNext
                    )
                }
            }
        }

        }

        if (isFullscreen && currentVideoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { isFullscreen = false }) {
                            Text("Salir", color = Color.White)
                        }
                        TextButton(onClick = { followsDeviceOrientation = !followsDeviceOrientation }) {
                            Text(if (followsDeviceOrientation) "Rotación automática" else "Permitir giro", color = Color.White)
                        }
                    }
                    Column {
                        PlaybackSeekBar(currentPositionMs, durationMs) { exoPlayer.seekTo(it) }
                        PlaybackControls(
                            isPlaying = isPlaying,
                            canGoPrevious = currentQueueIndex > 0,
                            canGoNext = currentQueueIndex in 0 until queue.lastIndex,
                            onPrevious = onPrevious,
                            onTogglePlayback = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                            onNext = onNext,
                            textColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return currentContext as? Activity
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
            Text("Atribución del icono: smashingstocks · Flaticon", style = MaterialTheme.typography.labelSmall)
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
    mediaKind: MediaKind = MediaKind.Audio,
    modifier: Modifier = Modifier,
    onPlayQueue: ((List<QueuedMediaItem>, startIndex: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var currentFolderUri by remember(folderUri) { mutableStateOf(folderUri) }

    val root = currentFolderUri?.let { Uri.parse(it) }?.let { DocumentFile.fromTreeUri(context, it) }
    val entries = root?.listFiles()
        ?.filter { MediaEntryVisibility.isVisible(it.name) }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
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
            Button(onClick = onChooseFolder, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                Text("Carpeta", style = MaterialTheme.typography.labelMedium)
            }
            if (folderUri != null) {
                Button(onClick = onClearFolder, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("Limpiar", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (folderUri != null && onPlayQueue != null) {
                Button(
                    onClick = {
                        val queue = buildCurrentFolderQueue(entries, currentFolderUri, mediaKind)
                        if (queue.isNotEmpty()) onPlayQueue(queue, 0)
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Reproducir", style = MaterialTheme.typography.labelMedium)
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
            Text(root?.name ?: title, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
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
                                    val queue = buildCurrentFolderQueue(entries, currentFolderUri, mediaKind)
                                    val startIndex = queue.indexOfFirst { it.uri == entry.uri }
                                    if (startIndex >= 0) onPlayQueue?.invoke(queue, startIndex)
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

private fun buildCurrentFolderQueue(
    entries: List<FolderEntry>,
    currentFolderUri: String?,
    mediaKind: MediaKind
): List<QueuedMediaItem> {
    return MediaQueuePlanner.buildQueue(
        entries.asSequence()
            .filterNot { it.isDirectory }
            .map {
                QueuedMediaItem(
                    uri = it.uri,
                    title = it.name,
                    sourceFolderUri = currentFolderUri,
                    kind = mediaKind
                )
            }
            .toList()
    )
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
