package sicnu.edu.cn.yangwencai2024110144.myapplication.lab7

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 实验七：音乐播放服务。
 *
 * 这个 Service 承担全部与播放有关的逻辑：
 *   1. 通过 ContentResolver 查询 [MediaStore] 中的音乐，得到歌曲列表；
 *   2. 用 MediaPlayer 完成播放、暂停、停止、上一首、下一首和拖动进度；
 *   3. 每换一首歌就在通知栏显示歌曲名。
 *
 * Activity 不需要重新查歌或自己创建 MediaPlayer，只要绑定服务拿到 [MusicBinder]，
 * 通过 Binder 上的方法读取歌曲和进度、下发播放命令即可。
 * 服务以 BIND_AUTO_CREATE 方式绑定，Activity 还在（包括按 Home 键退到后台）时
 * 服务就活着，音乐继续播放；Activity 真正销毁时解除绑定，服务随之销毁。
 *
 * 线程说明：
 *   MediaPlayer 的所有访问都在 [lock] 上同步，因此 Activity 的进度线程
 *   和主线程可以同时读取播放进度、执行控制命令而不会互相踩踏。
 */
class MusicPlayerService : Service() {

    private val lock = Any()

    /** MediaStore 查出来的播放列表；刷新时整体替换，不原地修改。 */
    private var songs: List<Song> = emptyList()

    private var currentIndex = -1
    private var player: MediaPlayer? = null
    private var prepared = false
    private var state = PlaybackState.STOPPED

    private val binder = MusicBinder()

    private enum class PlaybackState {
        STOPPED,
        PREPARING,
        PLAYING,
        PAUSED
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        songs = querySongs()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        synchronized(lock) {
            releasePlayerLocked()
            state = PlaybackState.STOPPED
        }
        cancelNotification()
        super.onDestroy()
    }

    /**
     * Binder 暴露给 Activity 的接口。
     * 方法内部统一加锁，保证主线程和进度线程读取的是同一份状态。
     */
    inner class MusicBinder : Binder() {

        fun songs(): List<Song> = synchronized(lock) { songs }

        fun currentIndex(): Int = synchronized(lock) { currentIndex }

        fun currentSong(): Song? = synchronized(lock) { songs.getOrNull(currentIndex) }

        fun isPlaying(): Boolean = synchronized(lock) { state == PlaybackState.PLAYING }

        fun isPrepared(): Boolean = synchronized(lock) { prepared }

        fun positionMs(): Int = synchronized(lock) {
            if (!prepared) return@synchronized 0
            try {
                player?.currentPosition?.coerceAtLeast(0) ?: 0
            } catch (e: IllegalStateException) {
                0
            }
        }

        fun durationMs(): Int = synchronized(lock) {
            if (prepared) {
                try {
                    val duration = player?.duration ?: 0
                    if (duration > 0) return@synchronized duration
                } catch (e: IllegalStateException) {
                    // MediaPlayer 状态变化期间读取失败，退回 MediaStore 的时长
                }
            }
            (songs.getOrNull(currentIndex)?.durationMs ?: 0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }

        fun playSong(index: Int) {
            synchronized(lock) { playLocked(index) }
        }

        fun togglePlayPause() {
            synchronized(lock) { toggleLocked() }
        }

        fun stopPlayback() {
            synchronized(lock) { stopLocked() }
        }

        fun playNext() {
            synchronized(lock) {
                if (songs.isNotEmpty()) playLocked(if (currentIndex < 0) 0 else currentIndex + 1)
            }
        }

        fun playPrevious() {
            synchronized(lock) {
                if (songs.isEmpty()) return@synchronized
                playLocked(if (currentIndex <= 0) songs.lastIndex else currentIndex - 1)
            }
        }

        fun seekTo(positionMs: Int) {
            synchronized(lock) {
                if (!prepared) return@synchronized
                try {
                    player?.seekTo(positionMs.coerceAtLeast(0))
                } catch (e: IllegalStateException) {
                    // 播放器正在切换歌曲，忽略这次拖动
                }
            }
        }

        /** 重新扫描 MediaStore，并把当前歌曲的索引对到新列表上。 */
        fun reloadSongs(): List<Song> = synchronized(lock) {
            val playingId = songs.getOrNull(currentIndex)?.id
            songs = querySongs()
            if (playingId != null) {
                val newIndex = songs.indexOfFirst { it.id == playingId }
                if (newIndex >= 0) {
                    currentIndex = newIndex
                } else {
                    // 正在播放的文件被删除了，停止播放
                    releasePlayerLocked()
                    state = PlaybackState.STOPPED
                    currentIndex = -1
                    cancelNotification()
                }
            } else if (currentIndex >= songs.size) {
                currentIndex = -1
            }
            songs
        }
    }

    // ==================== 播放控制（调用方已持有 lock） ====================

    private fun playLocked(index: Int) {
        if (songs.isEmpty()) return
        val safeIndex = ((index % songs.size) + songs.size) % songs.size
        currentIndex = safeIndex
        releasePlayerLocked()
        state = PlaybackState.PREPARING

        val song = songs[safeIndex]
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.setDataSource(this, song.uri)
            mediaPlayer.setOnPreparedListener { preparedPlayer ->
                val started = synchronized(lock) {
                    // 回调可能来自已经被替换掉的旧播放器，先确认它还是当前的
                    if (player !== preparedPlayer) {
                        return@synchronized false
                    }
                    prepared = true
                    try {
                        preparedPlayer.start()
                        state = PlaybackState.PLAYING
                        true
                    } catch (e: IllegalStateException) {
                        false
                    }
                }
                if (started) {
                    updateNotification(playing = true)
                }
            }
            mediaPlayer.setOnCompletionListener { completedPlayer ->
                // 一首放完自动播下一首；到了队尾就回到第一首
                synchronized(lock) {
                    if (player === completedPlayer) {
                        playLocked(currentIndex + 1)
                    }
                }
            }
            mediaPlayer.setOnErrorListener { failedPlayer, _, _ ->
                val wasCurrent = synchronized(lock) {
                    if (player !== failedPlayer) {
                        false
                    } else {
                        releasePlayerLocked()
                        state = PlaybackState.STOPPED
                        true
                    }
                }
                if (wasCurrent) {
                    cancelNotification()
                    Toast.makeText(
                        this@MusicPlayerService,
                        R.string.music_playback_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            releasePlayerLocked()
            state = PlaybackState.STOPPED
            Toast.makeText(
                this@MusicPlayerService,
                R.string.music_playback_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleLocked() {
        when (state) {
            PlaybackState.PLAYING -> {
                try {
                    player?.pause()
                } catch (e: IllegalStateException) {
                    return
                }
                state = PlaybackState.PAUSED
                updateNotification(playing = false)
            }

            PlaybackState.PAUSED -> {
                try {
                    player?.start()
                } catch (e: IllegalStateException) {
                    return
                }
                state = PlaybackState.PLAYING
                updateNotification(playing = true)
            }

            PlaybackState.PREPARING -> Unit

            PlaybackState.STOPPED -> {
                if (songs.isNotEmpty()) {
                    playLocked(if (currentIndex in songs.indices) currentIndex else 0)
                }
            }
        }
    }

    private fun stopLocked() {
        releasePlayerLocked()
        state = PlaybackState.STOPPED
        cancelNotification()
    }

    private fun releasePlayerLocked() {
        player?.let { mediaPlayer ->
            try {
                mediaPlayer.reset()
            } catch (e: IllegalStateException) {
                // 已经处于错误状态，直接释放
            }
            mediaPlayer.release()
        }
        player = null
        prepared = false
    }

    // ==================== ContentResolver 查询音乐列表 ====================

    /**
     * 通过 ContentResolver 查询 MediaStore 中的音乐。
     * 清单里已经声明 READ_MEDIA_AUDIO（Android 13+）或 READ_EXTERNAL_STORAGE（旧版本），
     * 没有授权时查询会抛 SecurityException，这里返回空列表，由界面提示申请权限。
     */
    private fun querySongs(): List<Song> {
        val result = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val title = cursor.getString(titleColumn)
                        ?.takeUnless { it.isBlank() || it == MediaStore.UNKNOWN_STRING }
                        ?: displayName?.substringBeforeLast('.')
                        ?: getString(R.string.music_unknown_song)
                    val artist = cursor.getString(artistColumn)
                        ?.takeUnless { it.isBlank() || it == MediaStore.UNKNOWN_STRING }
                        ?: getString(R.string.music_unknown_artist)
                    val album = cursor.getString(albumColumn)
                        ?.takeUnless { it.isBlank() || it == MediaStore.UNKNOWN_STRING }
                        ?: ""

                    result += Song(
                        id = id,
                        uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        ),
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = cursor.getLong(durationColumn).coerceAtLeast(0L)
                    )
                }
            }
        } catch (e: SecurityException) {
            // 没有读取音乐权限：返回空列表，界面会显示申请权限的提示
        }
        return result
    }

    // ==================== 通知栏 ====================

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.music_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.music_notification_channel_desc)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /** 每换一首歌都会调用它，把当前歌曲名写进通知栏。 */
    private fun updateNotification(playing: Boolean) {
        val song = synchronized(lock) { songs.getOrNull(currentIndex) } ?: return

        val tapIntent = Intent(this, MusicPlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song.title)
            .setContentText(getString(R.string.music_notification_artist, song.artist))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(playing)
        if (!playing) {
            builder.setSubText(getString(R.string.music_notification_paused))
        }
        val notification: Notification = builder.build()

        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Android 13+ 没有授予通知权限时通知不会显示，但播放本身不受影响
        }
    }

    private fun cancelNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val CHANNEL_ID = "music_playback"
        private const val NOTIFICATION_ID = 1001
    }
}
