package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6

import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore

/**
 * 第 2 个 Fragment：2 张牌的翻牌游戏。
 *
 * 每次翻开两张牌，点数相同即配对成功。
 * 牌桌使用 2 种花色，每组点数正好两张，保证所有牌都能配完。
 */
class TwoCardGameFragment : BaseCardGameFragment() {

    @get:IntegerRes
    override val cardCountRes: Int = R.integer.two_card_count

    @get:IntegerRes
    override val gridSpanRes: Int = R.integer.two_card_grid_span_count

    @get:StringRes
    override val gameHintRes: Int = R.string.game_two_hint

    @get:StringRes
    override val historyTitleRes: Int = R.string.history_title_two

    override val cardsInMatch: Int = 2

    override val historyFileName: String = GameHistoryStore.TWO_CARD_FILE
}
