package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 翻牌游戏的模型（Model 层）。
 *
 * 同一套规则支持两种玩法，由 [cardsInMatch] 决定：
 *   - 2 张牌玩法：两张点数相同即配对；
 *   - 3 张牌玩法：三张点数全部相同才配对。
 *
 * 通用规则：
 *   - 每翻开一张牌扣 1 分；
 *   - 连续翻开 [cardsInMatch] 张牌后自动判定，点数全部相同就配对成功，否则全部翻回去；
 *   - 配对成功得 [Card.RANK_MATCH_SCORE] × [MATCH_BONUS] 分；
 *     配对失败扣 [MISMATCH_PENALTY] 分。
 *
 * 牌桌使用 [cardsInMatch] 种花色、每种点数正好出现 [cardsInMatch] 次，
 * 所以理论上每一张牌都能被配完，不会出现永远配不掉的“死牌”。
 */
class CardMatchingGame private constructor(
    private val cards: MutableList<Card>,
    private val cardCount: Int,
    val cardsInMatch: Int
) : CardGameModel {

    constructor(count: Int, cardsInMatch: Int = Card.CARDS_IN_A_MATCH) : this(
        drawCards(count, cardsInMatch),
        count,
        cardsInMatch
    )

    init {
        require(cardsInMatch in 2..Card.validSuits.size) {
            "一次配对的牌数必须在 2 到 ${Card.validSuits.size} 之间"
        }
        require(cards.size >= cardsInMatch) {
            "牌桌上至少要有一组完整的牌"
        }
    }

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

        card.isChosen = true
        score -= COST_TO_CHOOSE

        val chosenCards = cards.filter { it.isChosen && !it.isMatched }
        if (chosenCards.size < cardsInMatch) {
            return
        }

        val matchScore = chosenCards.first()
            .match(chosenCards.drop(1).toTypedArray(), cardsInMatch)
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

    /** 重新发牌并清零分数；沿用原来的牌数和玩法。 */
    override fun reset() {
        cards.clear()
        cards.addAll(drawCards(cardCount, cardsInMatch))
        score = 0
    }

    /** 是否还存在可以配对的未配对牌。 */
    fun hasAvailableMatch(): Boolean {
        val unmatched = cards.filter { !it.isMatched }
        return hasMatchAmong(unmatched, 0, mutableListOf())
    }

    /**
     * 在 [cards] 中从 [start] 开始枚举 [cardsInMatch] 张牌的组合，
     * 一旦找到点数全部相同的一组就立即返回。
     */
    private fun hasMatchAmong(
        cards: List<Card>,
        start: Int,
        chosen: MutableList<Card>
    ): Boolean {
        if (chosen.size == cardsInMatch) {
            return chosen.first()
                .match(chosen.drop(1).toTypedArray(), cardsInMatch) > 0
        }

        val stillNeeded = cardsInMatch - chosen.size
        for (index in start..cards.size - stillNeeded) {
            chosen.add(cards[index])
            if (hasMatchAmong(cards, index + 1, chosen)) {
                return true
            }
            chosen.removeAt(chosen.size - 1)
        }
        return false
    }

    companion object {
        /** 三张牌玩法竖屏一局 24 张牌（8 组三张牌）。 */
        const val DEFAULT_CARD_COUNT = 24

        private fun drawCards(count: Int, cardsInMatch: Int): MutableList<Card> {
            require(cardsInMatch in 2..Card.validSuits.size) {
                "一次配对的牌数必须在 2 到 ${Card.validSuits.size} 之间"
            }
            require(count > 0 && count % cardsInMatch == 0) {
                "牌数必须是 $cardsInMatch 的正整数倍"
            }

            // 两张牌玩法只取两种花色，三张牌玩法取三种，保证每组牌数量正好。
            val deck = Deck(suits = Card.validSuits.take(cardsInMatch).toTypedArray())
            val drawn = mutableListOf<Card>()
            repeat(count / cardsInMatch) {
                val group = deck.drawRankGroup()
                drawn.addAll(group)
            }

            // 发牌后打乱位置，避免同一组牌总是挨在一起。
            return drawn.shuffled().toMutableList()
        }

        /**
         * 单元测试专用：用指定牌面构造模型。
         * 随机牌组会让测试结果不可预期，这里给测试一个确定性的入口。
         */
        internal fun withCards(
            cards: List<Card>,
            cardsInMatch: Int = Card.CARDS_IN_A_MATCH
        ): CardMatchingGame {
            return CardMatchingGame(cards.toMutableList(), cards.size, cardsInMatch)
        }
    }
}
