package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 一张扑克牌（Model 层）。
 *
 * 只负责牌本身的数据与配对规则，不认识 Android 的任何类，
 * 因此可以直接放在 JVM 单元测试里运行。
 *
 * 实验六同时保留两种玩法：
 *   - 两张牌游戏：两张点数相同时配对；
 *   - 三张牌游戏：三张点数全部相同时配对。
 * 配几张牌由 [match] 的 cardsInMatch 参数决定，牌面数据本身与玩法无关。
 *
 * 注释说明：花色符号与点数是牌面数据，不是界面文案，
 * 所以留在模型里，不放进 strings.xml。
 */
class Card(
    val suit: String,
    val rank: String,
    var isChosen: Boolean = false,
    var isMatched: Boolean = false
) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")

        /** 三张牌玩法一次配对需要翻开的牌数 */
        const val CARDS_IN_A_MATCH = 3

        /** 三张牌点数相同时的基础分 */
        const val RANK_MATCH_SCORE = 4

        /** 三张牌不能配对 */
        const val NO_MATCH_SCORE = 0
    }

    override fun toString(): String {
        return suit + rank
    }

    /**
     * 与同组其他牌比较，判断这一组能否配对。
     *
     * [cardsInMatch] 是一组需要翻开的牌数：两张牌玩法传 2，三张牌玩法传 3。
     * 同组所有牌点数相同时得 4 分，否则得 0 分；
     * 传入的牌数与玩法不符时一律视为不匹配。
     */
    fun match(otherCards: Array<Card>, cardsInMatch: Int = CARDS_IN_A_MATCH): Int {
        if (cardsInMatch < 2 || otherCards.size != cardsInMatch - 1) {
            return NO_MATCH_SCORE
        }
        val group = listOf(this) + otherCards
        return if (group.all { it.rank == rank }) RANK_MATCH_SCORE else NO_MATCH_SCORE
    }
}
