package sicnu.edu.cn.yangwencai2024110144.myapplication.lab7

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ItemSongBinding

/**
 * 实验七：播放列表适配器。
 *
 * 点击整行把位置回调给 Activity；正在播放的那一行用主题色高亮。
 */
class SongAdapter(
    private val onSongClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var songs: List<Song> = emptyList()
    private var currentIndex = -1

    fun submitSongs(newSongs: List<Song>) {
        songs = newSongs
        currentIndex = -1
        notifyDataSetChanged()
    }

    /** 播放服务切换歌曲后，同步刷新列表里的高亮。 */
    fun setCurrentIndex(index: Int) {
        if (index == currentIndex) return
        val previous = currentIndex
        currentIndex = index
        if (previous in songs.indices) notifyItemChanged(previous)
        if (index in songs.indices) notifyItemChanged(index)
    }

    override fun getItemCount(): Int = songs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position, position == currentIndex)
        holder.binding.root.setOnClickListener {
            val clicked = holder.bindingAdapterPosition
            if (clicked != RecyclerView.NO_POSITION) onSongClick(clicked)
        }
    }

    inner class SongViewHolder(
        val binding: ItemSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int, selected: Boolean) {
            val context = binding.root.context
            binding.tvTrackNumber.text = (position + 1).toString()
            binding.tvSongTitle.text = song.title
            binding.tvSongArtist.text = song.artist
            binding.tvSongDuration.text =
                DateUtils.formatElapsedTime((song.durationMs / 1000L).coerceAtLeast(0L))

            val accent = ContextCompat.getColor(context, R.color.brand_primary)
            val trackBackground =
                ContextCompat.getColor(context, if (selected) R.color.brand_primary else R.color.avatar_bg)
            binding.tvTrackNumber.backgroundTintList = ColorStateList.valueOf(trackBackground)
            binding.tvTrackNumber.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (selected) R.color.white else R.color.brand_primary
                )
            )
            binding.tvSongTitle.setTextColor(
                if (selected) accent else ContextCompat.getColor(context, R.color.text_primary)
            )
        }
    }
}
