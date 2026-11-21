package sicnu.edu.cn.yangwencai2024110144.myapplication.lab7

import android.net.Uri

/**
 * 实验七：一首本地音乐。
 *
 * 数据全部来自 [android.provider.MediaStore]，
 * [uri] 是播放时交给 MediaPlayer 的内容 URI，而不是文件路径。
 */
data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long
)
