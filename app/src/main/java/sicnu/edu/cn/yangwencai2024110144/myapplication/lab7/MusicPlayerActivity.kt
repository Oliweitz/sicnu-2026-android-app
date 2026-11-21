package sicnu.edu.cn.yangwencai2024110144.myapplication.lab7

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.text.format.DateUtils
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityMusicPlayerBinding

/**
 * 实验七：音乐播放器界面。
 *
 * Activity 自己不持有 MediaPlayer，而是绑定 [MusicPlayerService]：
 *   - 通过 Binder 获取歌曲列表、当前歌曲、播放状态和进度；
 *   - 播放 / 暂停 / 停止 / 上一首 / 下一首 / 拖动进度全部交给服务执行。
 *
 * SeekBar 的进度由一个独立的 [HandlerThread] 每 500 毫秒读取一次 Binder，
 * 再把结果投递回主线程刷新界面，因此界面不会被阻塞；
 * 用户拖动时暂停自动刷新，松手后调用服务的 seekTo 修改播放位置。
 */
class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMusicPlayerBinding
    private val songAdapter = SongAdapter(::onSongClicked)

    @Volatile
    private var musicBinder: MusicPlayerService.MusicBinder? = null
    private var bound = false

    private var isUserSeeking = false
    private var lastRenderedIndex = Int.MIN_VALUE
    private var lastPlayingIcon: Boolean? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val progressThread = HandlerThread("music-progress").apply { start() }
    private val progressHandler = Handler(progressThread.looper)

    @Volatile
    private var progressRunning = false

    /** 后台线程定时读取一次播放状态，再回到主线程刷新界面。 */
    private val progressTick = object : Runnable {
        override fun run() {
            pollPlaybackState()
            if (progressRunning) {
                progressHandler.postDelayed(this, PROGRESS_INTERVAL_MS)
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicPlayerService.MusicBinder
            if (binder == null) {
                Toast.makeText(
                    this@MusicPlayerActivity,
                    R.string.music_service_error,
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            musicBinder = binder
            lastRenderedIndex = Int.MIN_VALUE
            updatePlaylist(binder.songs())
            pollPlaybackState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBinder = null
            renderPlayback(-1, null, false, 0, 0)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val notificationDenied = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            result[Manifest.permission.POST_NOTIFICATIONS] == false
        if (notificationDenied) {
            Toast.makeText(
                this,
                R.string.music_notification_permission_denied,
                Toast.LENGTH_SHORT
            ).show()
        }

        if (hasAudioPermission()) {
            showLoadingState()
            bindToService()
        } else {
            showPermissionState()
            Toast.makeText(this, R.string.music_permission_denied, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        // 让歌名在放不下时滚动显示
        binding.tvSongTitle.isSelected = true

        setupPlaylist()
        setupSeekBar()
        setupControls()
        renderPlayback(-1, null, false, 0, 0)

        if (hasAudioPermission()) {
            showLoadingState()
            bindToService()
        } else {
            showPermissionState()
            requestMusicPermissions()
        }
    }

    override fun onStart() {
        super.onStart()
        startProgressUpdates()
    }

    override fun onStop() {
        stopProgressUpdates()
        super.onStop()
    }

    override fun onDestroy() {
        stopProgressUpdates()
        progressThread.quitSafely()
        mainHandler.removeCallbacksAndMessages(null)
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        musicBinder = null
        super.onDestroy()
    }

    // ==================== 界面初始化 ====================

    private fun setupPlaylist() {
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = songAdapter
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // 拖动期间停止自动刷新，否则手指还没松开进度条就会被弹回去
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isUserSeeking = false
                musicBinder?.seekTo(seekBar.progress)
                pollPlaybackState()
            }
        })
    }

    private fun setupControls() {
        binding.fabPlayPause.setOnClickListener {
            musicBinder?.togglePlayPause()
            pollPlaybackState()
        }
        binding.btnStop.setOnClickListener {
            musicBinder?.stopPlayback()
            pollPlaybackState()
        }
        binding.btnPrevious.setOnClickListener {
            musicBinder?.playPrevious()
            pollPlaybackState()
        }
        binding.btnNext.setOnClickListener {
            musicBinder?.playNext()
            pollPlaybackState()
        }
        binding.btnRefresh.setOnClickListener { refreshPlaylistFromService() }
    }

    // ==================== 绑定服务 ====================

    private fun bindToService() {
        if (bound) return
        val intent = Intent(this, MusicPlayerService::class.java)
        bound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            Toast.makeText(this, R.string.music_service_error, Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== 进度刷新 ====================

    private fun startProgressUpdates() {
        if (progressRunning) return
        progressRunning = true
        progressHandler.post(progressTick)
    }

    private fun stopProgressUpdates() {
        progressRunning = false
        progressHandler.removeCallbacks(progressTick)
    }

    /** 运行在进度线程：只读取数据，不碰界面。 */
    private fun pollPlaybackState() {
        val binder = musicBinder ?: return
        val index = binder.currentIndex()
        val song = binder.currentSong()
        val playing = binder.isPlaying()
        val position = binder.positionMs()
        val duration = binder.durationMs()

        mainHandler.post {
            if (!isFinishing && !isDestroyed) {
                renderPlayback(index, song, playing, position, duration)
            }
        }
    }

    private fun renderPlayback(
        index: Int,
        song: Song?,
        playing: Boolean,
        positionMs: Int,
        durationMs: Int
    ) {
        if (song == null) {
            if (lastRenderedIndex != -1) {
                binding.tvSongTitle.setText(R.string.music_nothing_playing)
                binding.tvSongArtist.setText(R.string.music_unknown_artist)
                songAdapter.setCurrentIndex(-1)
                lastRenderedIndex = -1
            }
        } else if (index != lastRenderedIndex) {
            binding.tvSongTitle.text = song.title
            binding.tvSongArtist.text = song.artist
            songAdapter.setCurrentIndex(index)
            (binding.rvSongs.layoutManager as? LinearLayoutManager)?.scrollToPosition(index)
            lastRenderedIndex = index
        }

        if (song == null) {
            if (!isUserSeeking) {
                binding.seekBar.max = 1
                binding.seekBar.progress = 0
                binding.tvCurrentTime.text = formatTime(0)
            }
            binding.tvTotalTime.text = formatTime(0)
        } else {
            val total = durationMs.coerceAtLeast(0)
            val max = total.coerceAtLeast(1)
            if (binding.seekBar.max != max) {
                binding.seekBar.max = max
            }
            if (!isUserSeeking) {
                binding.seekBar.progress = positionMs.coerceIn(0, max)
                binding.tvCurrentTime.text = formatTime(positionMs)
            }
            binding.tvTotalTime.text = formatTime(total)
        }

        if (lastPlayingIcon != playing) {
            binding.fabPlayPause.setImageResource(
                if (playing) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.fabPlayPause.contentDescription = getString(
                if (playing) R.string.music_pause else R.string.music_play
            )
            lastPlayingIcon = playing
        }
        updateControlsEnabled()
    }

    private fun updateControlsEnabled() {
        val enabled = musicBinder != null && songAdapter.itemCount > 0
        binding.fabPlayPause.isEnabled = enabled
        binding.btnPrevious.isEnabled = enabled
        binding.btnStop.isEnabled = enabled
        binding.btnNext.isEnabled = enabled
        binding.seekBar.isEnabled = enabled
        val alpha = if (enabled) 1f else 0.4f
        binding.fabPlayPause.alpha = alpha
        binding.btnPrevious.alpha = alpha
        binding.btnStop.alpha = alpha
        binding.btnNext.alpha = alpha
    }

    // ==================== 播放列表 ====================

    private fun updatePlaylist(songs: List<Song>) {
        songAdapter.submitSongs(songs)
        binding.tvSongCount.text = getString(R.string.music_song_count, songs.size)
        binding.progressLoading.visibility = View.GONE
        if (songs.isEmpty()) {
            binding.rvSongs.visibility = View.GONE
            showNoSongsState()
        } else {
            binding.rvSongs.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    /** 重新扫描 MediaStore，查询放在进度线程上做，避免卡住界面。 */
    private fun refreshPlaylistFromService() {
        val binder = musicBinder ?: run {
            requestMusicPermissions()
            return
        }
        showLoadingState()
        progressHandler.post {
            val songs = binder.reloadSongs()
            mainHandler.post {
                if (isFinishing || isDestroyed) return@post
                lastRenderedIndex = Int.MIN_VALUE
                updatePlaylist(songs)
                pollPlaybackState()
            }
        }
    }

    private fun onSongClicked(index: Int) {
        musicBinder?.playSong(index)
        pollPlaybackState()
    }

    // ==================== 空状态与权限 ====================

    private fun showLoadingState() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showPermissionState() {
        binding.progressLoading.visibility = View.GONE
        binding.rvSongs.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        binding.tvEmptyTitle.setText(R.string.music_permission_title)
        binding.tvEmptyHint.setText(R.string.music_permission_hint)
        binding.btnEmptyAction.setText(R.string.music_grant_permission)
        binding.btnEmptyAction.setOnClickListener { requestMusicPermissions() }
        songAdapter.submitSongs(emptyList())
        binding.tvSongCount.text = getString(R.string.music_song_count, 0)
        updateControlsEnabled()
    }

    private fun showNoSongsState() {
        binding.progressLoading.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
        binding.tvEmptyTitle.setText(R.string.music_no_songs_title)
        binding.tvEmptyHint.setText(R.string.music_no_songs_hint)
        binding.btnEmptyAction.setText(R.string.music_refresh)
        binding.btnEmptyAction.setOnClickListener { refreshPlaylistFromService() }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            audioPermission()
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun requestMusicPermissions() {
        val permissions = mutableListOf(audioPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 通知栏显示歌曲名同样需要运行时权限
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun formatTime(ms: Int): String {
        return DateUtils.formatElapsedTime((ms / 1000L).coerceAtLeast(0L))
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 500L
    }
}
