package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 翻牌游戏的模型（Model 层），依据实验附件代码实现。
 *
 * 计分规则：
 *   - 每翻开一张牌扣 1 分；
 *   - 两张牌点数相同：基础分 4 × MATCH_BONUS，配对的牌保持翻开状态；
 *   - 只有花色相同：基础分 1 × MATCH_BONUS；
 *   - 两张牌不匹配：扣 MISMATCH_PENALTY，并自动翻回先选的那张。
 */
class CardMatchingGame private constructor(
    private val cards: MutableList<Card>
) : CardGameModel {

    constructor(count: Int) : this(drawCards(count))

    override var score = 0
        private set

    override val numberOfCards: Int
        get() = cards.size

    /**
     * 全部配完，或者剩余未配对的牌之间已经没有任何可能的配对时，本局结束。
     * 后一种情况必须单独判断，否则玩家会发现有些牌永远配不上、游戏却停不下来。
     */
    override val isGameOver: Boolean
        get() = cards.all { it.isMatched } || !hasAvailableMatch()

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    override fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    override fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) {
            return
        }
        if (card.isChosen) {
            // 再次点击已经翻开的牌：翻回去，不扣分
            card.isChosen = false
            return
        }

        for (otherCard in cards) {
            if (otherCard.isChosen && !otherCard.isMatched) {
                val matchScore = card.match(arrayOf(otherCard))
                if (matchScore > 0) {
                    score += matchScore * MATCH_BONUS
                    otherCard.isMatched = true
                    card.isMatched = true
                } else {
                    score -= MISMATCH_PENALTY
                    otherCard.isChosen = false
                }
                break
            }
        }

        score -= COST_TO_CHOOSE
        card.isChosen = true
    }

    /** 是否还存在可以配对的未翻开牌。 */
    fun hasAvailableMatch(): Boolean {
        for (i in cards.indices) {
            if (cards[i].isMatched) continue
            for (j in i + 1 until cards.size) {
                if (!cards[j].isMatched && cards[i].match(arrayOf(cards[j])) > 0) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private fun drawCards(count: Int): MutableList<Card> {
            val deck = Deck()
            val drawn = mutableListOf<Card>()
            repeat(count) {
                deck.drawRandomCard()?.let(drawn::add)
            }
            return drawn
        }

        /**
         * 单元测试专用：用指定牌面构造模型。
         * 随机牌组会让测试结果不可预期，这里给测试一个确定性的入口。
         */
        internal fun withCards(cards: List<Card>): CardMatchingGame {
            return CardMatchingGame(cards.toMutableList())
        }
    }
}
