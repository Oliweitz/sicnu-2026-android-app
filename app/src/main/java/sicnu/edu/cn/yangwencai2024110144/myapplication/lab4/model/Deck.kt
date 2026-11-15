package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import java.util.Random

/**
 * 一副牌（Model 层）。
 *
 * 默认是 52 张标准扑克牌；翻牌游戏只需要与“一次配对牌数”相同数量的花色，
 * 因此 [CardMatchingGame] 会用 [suits] 参数构造牌堆：
 * 两张牌玩法是 26 张（2 种花色），三张牌玩法是 39 张（3 种花色）。
 *
 * 抽牌时从剩余牌中随机取走，保证同一局里不会出现重复的牌；
 * [drawRankGroup] 一次取走同一点数的全部牌，用来保证每一组牌都能配完。
 */
class Deck(
    suits: Array<String> = Card.validSuits,
    ranks: Array<String> = Card.rankStrings
) {
    private val cards = mutableListOf<Card>()
    private val random = Random()

    init {
        for (suit in suits) {
            for (rank in ranks) {
                cards.add(Card(suit = suit, rank = rank))
            }
        }
    }

    fun drawRandomCard(): Card? {
        var randomCard: Card? = null
        if (cards.size > 0) {
            randomCard = cards.removeAt(random.nextInt(cards.size))
        }
        return randomCard
    }

    /**
     * 随机挑一个点数，把该点数的整组牌全部取走。
     * 牌堆有几种花色，每组就正好有几张，适合按组发牌。
     */
    fun drawRankGroup(): List<Card> {
        if (cards.isEmpty()) {
            return emptyList()
        }
        val rank = cards[random.nextInt(cards.size)].rank
        val group = cards.filter { it.rank == rank }
        cards.removeAll(group)
        return group
    }
}
