package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6

import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore

/**
 * 第 3 个 Fragment：3 张牌的翻牌游戏。
 *
 * 沿用实验五的规则：每次翻开三张牌，三张点数全部相同才配对成功。
 * 牌桌使用 3 种花色，每组点数正好三张，保证所有牌都能配完。
 */
class ThreeCardGameFragment : BaseCardGameFragment() {

    @get:IntegerRes
    override val cardCountRes: Int = R.integer.game_card_count

    @get:IntegerRes
    override val gridSpanRes: Int = R.integer.game_grid_span_count

    @get:StringRes
    override val gameHintRes: Int = R.string.game_three_hint

    @get:StringRes
    override val historyTitleRes: Int = R.string.history_title_three

    override val cardsInMatch: Int = 3

    override val historyFileName: String = GameHistoryStore.THREE_CARD_FILE
}
