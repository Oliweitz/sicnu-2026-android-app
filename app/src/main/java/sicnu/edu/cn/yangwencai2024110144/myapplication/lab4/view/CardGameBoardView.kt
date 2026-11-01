package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout
import sicnu.edu.cn.yangwencai2024110144.myapplication.R

/**
 * 4 × 4 的牌桌（View 层）。
 *
 * 只负责创建/更新 16 张 [PlayingCardView]，并把点击的牌序号交给 Controller。
 * 牌面的排列、翻牌动画、无障碍描述都在这里完成，游戏规则不在这里。
 */
class CardGameBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GridLayout(context, attrs), CardGameView {

    override var onCardSelected: ((Int) -> Unit)? = null

    private val tiles = mutableListOf<PlayingCardView>()
    private val cellMargin = dp(5)

    init {
        columnCount = COLUMN_COUNT
        rowCount = ROW_COUNT
        alignmentMode = ALIGN_BOUNDS
        useDefaultMargins = false
        clipToPadding = false
        setPadding(dp(8), dp(4), dp(8), dp(4))
    }

    override fun renderCards(cards: List<CardUiState>) {
        ensureTiles(cards.size)
        cards.forEachIndexed { position, state ->
            tiles[position].bind(state)
        }
    }

    private fun ensureTiles(count: Int) {
        if (tiles.size == count) return

        removeAllViews()
        tiles.clear()

        for (index in 0 until count) {
            val tile = PlayingCardView(context).apply {
                id = CARD_IDS.getOrNull(index) ?: View.generateViewId()
                setOnClickListener { onCardSelected?.invoke(index) }
            }
            val params = LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
            }
            addView(tile, params)
            tiles.add(tile)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val COLUMN_COUNT = 4
        private const val ROW_COUNT = 4

        private val CARD_IDS = intArrayOf(
            R.id.card_1, R.id.card_2, R.id.card_3, R.id.card_4,
            R.id.card_5, R.id.card_6, R.id.card_7, R.id.card_8,
            R.id.card_9, R.id.card_10, R.id.card_11, R.id.card_12,
            R.id.card_13, R.id.card_14, R.id.card_15, R.id.card_16
        )
    }
}
