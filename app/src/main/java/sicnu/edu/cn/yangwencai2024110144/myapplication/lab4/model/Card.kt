package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 一张扑克牌（Model 层）。
 *
 * 只负责牌本身的数据与配对规则，不认识 Android 的任何类，
 * 因此可以直接放在 JVM 单元测试里运行。
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

        /** 点数相同时的基础分 */
        const val RANK_MATCH_SCORE = 4

        /** 只有花色相同时的基础分 */
        const val SUIT_MATCH_SCORE = 1

        /** 既不同点数也不同花色 */
        const val NO_MATCH_SCORE = 0
    }

    override fun toString(): String {
        return suit + rank
    }

    /**
     * 与另一张牌比较。
     * 点数相同得 4 分，否则花色相同得 1 分，都不相同得 0 分。
     */
    fun match(otherCards: Array<Card>): Int {
        var score = 0
        if (otherCards.size == 1) {
            val otherCard = otherCards[0]
            if (otherCard.rank == rank) {
                score = RANK_MATCH_SCORE
            } else if (otherCard.suit == suit) {
                score = SUIT_MATCH_SCORE
            }
        }
        return score
    }
}
