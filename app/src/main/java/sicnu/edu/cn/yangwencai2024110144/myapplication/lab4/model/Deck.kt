package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import java.util.Random

/**
 * 一副牌（Model 层）。
 *
 * 默认是 52 张标准扑克牌；三张牌游戏只需要三种花色，
 * 因此 [CardMatchingGame] 会用 [suits] 参数构造一副 39 张的牌堆。
 *
 * 抽牌时从剩余牌中随机取走，保证同一局里不会出现重复的牌；
 * [drawRankGroup] 一次取走同一点数的全部牌，用来保证每一组三张牌都能配完。
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
     * 三花色牌堆里每组正好三张，适合三张牌游戏发牌。
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
