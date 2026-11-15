package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage

import java.io.Serializable

/**
 * 一局已经结束的翻牌游戏的记录。
 *
 * 记录会随 [GameHistoryStore] 一起做对象序列化，
 * 所以退出应用后重新打开，历史列表仍然可以恢复。
 */
data class GameRecord(
    /** 本局最终得分（可能为负数）。 */
    val score: Int,
    /** 结束时间，毫秒时间戳，用于在历史列表中显示日期。 */
    val playedAt: Long,
    /** 本局牌桌上的牌数。 */
    val cardCount: Int,
    /** 本局一次配对需要翻开的牌数：2 张牌玩法为 2，三张牌玩法为 3。 */
    val cardsInMatch: Int
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
