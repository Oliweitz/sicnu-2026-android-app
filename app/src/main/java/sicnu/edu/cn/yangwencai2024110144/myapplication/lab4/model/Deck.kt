package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import java.util.Random

/**
 * 一副 52 张的牌（Model 层）。
 * 抽牌时从剩余牌中随机取走一张，保证同一局里不会出现重复的牌。
 */
class Deck {
    private val cards = mutableListOf<Card>()
    private val random = Random()

    init {
        for (suit in Card.validSuits) {
            for (rank in Card.rankStrings) {
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
}
