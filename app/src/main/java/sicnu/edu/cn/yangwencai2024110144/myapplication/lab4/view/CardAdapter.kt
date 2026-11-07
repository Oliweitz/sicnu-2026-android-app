package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ItemPlayingCardBinding

/**
 * 牌桌的 RecyclerView 适配器（View 层）。
 *
 * 除了标准的 onCreateViewHolder / onBindViewHolder / getItemCount，
 * 还实现了 [CardGameView] 接口：Controller 调用 [renderCards] 下发状态，
 * 适配器把点击通过 [onCardSelected] 回调给 Controller。
 *
 * 只更新真正发生变化的牌，避免每次翻牌都重绘整桌。
 */
class CardAdapter : RecyclerView.Adapter<CardAdapter.CardViewHolder>(), CardGameView {

    override var onCardSelected: ((Int) -> Unit)? = null

    private var cards: List<CardUiState> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemPlayingCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding) { position -> onCardSelected?.invoke(position) }
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], position)
    }

    override fun getItemCount(): Int = cards.size

    override fun renderCards(cards: List<CardUiState>) {
        val previous = this.cards
        this.cards = cards

        if (previous.size != cards.size) {
            notifyDataSetChanged()
            return
        }

        for (index in cards.indices) {
            if (previous[index] != cards[index]) {
                notifyItemChanged(index)
            }
        }
    }

    class CardViewHolder(
        private val binding: ItemPlayingCardBinding,
        onCardClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardClick(position)
                }
            }
        }

        fun bind(state: CardUiState, position: Int) {
            // UI 测试通过固定的 card_1 ... card_24 定位“第几张牌”。
            binding.cardView.id = cardIdFor(position)
            binding.cardView.bind(state)
        }
    }

    private companion object {
        val CARD_IDS = intArrayOf(
            R.id.card_1, R.id.card_2, R.id.card_3, R.id.card_4,
            R.id.card_5, R.id.card_6, R.id.card_7, R.id.card_8,
            R.id.card_9, R.id.card_10, R.id.card_11, R.id.card_12,
            R.id.card_13, R.id.card_14, R.id.card_15, R.id.card_16,
            R.id.card_17, R.id.card_18, R.id.card_19, R.id.card_20,
            R.id.card_21, R.id.card_22, R.id.card_23, R.id.card_24
        )

        fun cardIdFor(position: Int): Int {
            return CARD_IDS.getOrElse(position) { View.generateViewId() }
        }
    }
}
