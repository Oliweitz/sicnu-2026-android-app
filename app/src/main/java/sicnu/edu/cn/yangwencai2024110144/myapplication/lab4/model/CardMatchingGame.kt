package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 三张牌翻牌游戏的模型（Model 层）。
 *
 * 玩法：
 *   - 每翻开一张牌扣 1 分；
 *   - 连续翻开三张牌后自动判定，三张点数相同就配对成功，否则全部翻回去；
 *   - 配对成功得 4 × MATCH_BONUS 分；配对失败扣 MISMATCH_PENALTY 分。
 *
 * 牌桌使用三种花色、每张牌的点数在牌桌内正好出现三次，
 * 所以理论上每一张牌都能被配完，不会出现永远配不掉的“死牌”。
 */
class CardMatchingGame private constructor(
    private val cards: MutableList<Card>,
    private val cardCount: Int
) : CardGameModel {

    constructor(count: Int) : this(drawCards(count), count)

    override var score = 0
        private set

    override val numberOfCards: Int
        get() = cards.size

    /**
     * 全部配完，或者剩余未配对的牌之间已经没有任何可能的三张配对时，本局结束。
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

        card.isChosen = true
        score -= COST_TO_CHOOSE

        val chosenCards = cards.filter { it.isChosen && !it.isMatched }
        if (chosenCards.size < Card.CARDS_IN_A_MATCH) {
            return
        }

        val matchScore = chosenCards.first().match(chosenCards.drop(1).toTypedArray())
        if (matchScore > 0) {
            score += matchScore * MATCH_BONUS
            for (matchedCard in chosenCards) {
                matchedCard.isMatched = true
            }
        } else {
            score -= MISMATCH_PENALTY
            for (flippedCard in chosenCards) {
                flippedCard.isChosen = false
            }
        }
    }

    /** 重新发牌并清零分数；三张牌游戏沿用原来的牌数。 */
    override fun reset() {
        cards.clear()
        cards.addAll(drawCards(cardCount))
        score = 0
    }

    /** 是否还存在可以配对的未配对牌。 */
    fun hasAvailableMatch(): Boolean {
        val unmatched = cards.filter { !it.isMatched }
        for (i in unmatched.indices) {
            for (j in i + 1 until unmatched.size) {
                for (k in j + 1 until unmatched.size) {
                    if (unmatched[i].match(arrayOf(unmatched[j], unmatched[k])) > 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    companion object {
        /** 竖屏一局 24 张牌（8 组三张牌）。 */
        const val DEFAULT_CARD_COUNT = 24

        private fun drawCards(count: Int): MutableList<Card> {
            require(count > 0 && count % Card.CARDS_IN_A_MATCH == 0) {
                "牌数必须是 ${Card.CARDS_IN_A_MATCH} 的正整数倍"
            }

            // 只取三种花色，保证每个点数正好三张，三张一对可以全部配完。
            val deck = Deck(suits = Card.validSuits.take(Card.CARDS_IN_A_MATCH).toTypedArray())
            val drawn = mutableListOf<Card>()
            repeat(count / Card.CARDS_IN_A_MATCH) {
                val group = deck.drawRankGroup()
                drawn.addAll(group)
            }

            // 发牌后打乱位置，避免同一组三张牌总是挨在一起。
            return drawn.shuffled().toMutableList()
        }

        /**
         * 单元测试专用：用指定牌面构造模型。
         * 随机牌组会让测试结果不可预期，这里给测试一个确定性的入口。
         */
        internal fun withCards(cards: List<Card>): CardMatchingGame {
            return CardMatchingGame(cards.toMutableList(), cards.size)
        }
    }
}
