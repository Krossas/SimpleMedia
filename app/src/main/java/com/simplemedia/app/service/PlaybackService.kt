package com.simplemedia.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var currentTitle: String = "SimpleMedia"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_PLAY -> {
                    player?.play()
                    updateNotification(true)
                    return START_STICKY
                }
                ACTION_PAUSE -> {
                    player?.pause()
                    updateNotification(false)
                    return START_STICKY
                }
                ACTION_STOP -> {
                    player?.stop()
                    player?.clearMediaItems()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
        }

        val uri = intent?.getStringExtra(EXTRA_URI) ?: return START_NOT_STICKY
        currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: "SimpleMedia"

        val mediaItem = MediaItem.fromUri(uri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
        updateNotification(true)

        return START_STICKY
    }

    override fun onDestroy() {
        mediaSession?.release()
        player?.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private fun updateNotification(isPlaying: Boolean) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTitle)
            .setContentText(if (isPlaying) "Reproduciendo audio local" else "Pausado")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pausar" else "Reproducir",
                buildActionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Detener",
                buildActionIntent(ACTION_STOP)
            )
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun buildActionIntent(action: String): android.app.PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).setAction(action)
        return android.app.PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de audio",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun start(context: Context, uri: String, title: String) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                putExtra(EXTRA_URI, uri)
                putExtra(EXTRA_TITLE, title)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun play(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java).setAction(ACTION_PLAY))
        }

        fun pause(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java).setAction(ACTION_PAUSE))
        }

        fun stop(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java).setAction(ACTION_STOP))
        }

        const val EXTRA_URI = "extra_uri"
        const val EXTRA_TITLE = "extra_title"
        private const val CHANNEL_ID = "simplemedia_audio_playback"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_PLAY = "com.simplemedia.app.action.PLAY"
        private const val ACTION_PAUSE = "com.simplemedia.app.action.PAUSE"
        private const val ACTION_STOP = "com.simplemedia.app.action.STOP"
    }
}
