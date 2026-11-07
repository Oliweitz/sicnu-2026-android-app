package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 一张扑克牌（Model 层）。
 *
 * 只负责牌本身的数据与配对规则，不认识 Android 的任何类，
 * 因此可以直接放在 JVM 单元测试里运行。
 *
 * 实验五把玩法从“两张配对”改成了“三张配对”：
 * 三张牌的点数全部相同时才算配对成功。牌桌由三种花色组成，
 * 每种点数正好三张，所以每一组牌都有机会被配完。
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

        /** 一次配对需要翻开的牌数 */
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
     * 与另外两张牌比较，判断三张牌能否配对。
     *
     * 参数必须正好是两张牌（加上自己共三张）；三张牌点数全部相同得 4 分，
     * 否则得 0 分。传入其他张数时一律视为不匹配。
     */
    fun match(otherCards: Array<Card>): Int {
        if (otherCards.size != CARDS_IN_A_MATCH - 1) {
            return NO_MATCH_SCORE
        }
        val group = listOf(this) + otherCards
        return if (group.all { it.rank == rank }) RANK_MATCH_SCORE else NO_MATCH_SCORE
    }
}
