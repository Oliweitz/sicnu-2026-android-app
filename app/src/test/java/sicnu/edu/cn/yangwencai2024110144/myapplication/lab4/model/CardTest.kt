package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Card 的两张牌 / 三张牌配对规则单元测试。
 * Card 不依赖 Android，因此这些测试在本地 JVM 上直接运行。
 */
class CardTest {

    @Test
    fun twoCardsWithSameRankMatchWhenPlayingPairs() {
        val heartAce = Card(suit = "♥", rank = "A")
        val spadeAce = Card(suit = "♠", rank = "A")

        assertEquals(
            Card.RANK_MATCH_SCORE,
            heartAce.match(arrayOf(spadeAce), cardsInMatch = 2)
        )
    }

    @Test
    fun twoCardsWithDifferentRanksDoNotMatchWhenPlayingPairs() {
        val heartAce = Card(suit = "♥", rank = "A")
        val diamondKing = Card(suit = "♦", rank = "K")

        assertEquals(
            Card.NO_MATCH_SCORE,
            heartAce.match(arrayOf(diamondKing), cardsInMatch = 2)
        )
    }

    @Test
    fun pairRuleRejectsThreeCards() {
        val heartAce = Card(suit = "♥", rank = "A")
        val diamondAce = Card(suit = "♦", rank = "A")
        val spadeAce = Card(suit = "♠", rank = "A")

        assertEquals(
            Card.NO_MATCH_SCORE,
            heartAce.match(arrayOf(diamondAce, spadeAce), cardsInMatch = 2)
        )
    }

    @Test
    fun threeCardsWithSameRankMatch() {
        val heartAce = Card(suit = "♥", rank = "A")
        val diamondAce = Card(suit = "♦", rank = "A")
        val spadeAce = Card(suit = "♠", rank = "A")

        assertEquals(
            Card.RANK_MATCH_SCORE,
            heartAce.match(arrayOf(diamondAce, spadeAce))
        )
    }

    @Test
    fun threeCardsWithDifferentRanksDoNotMatch() {
        val heartAce = Card(suit = "♥", rank = "A")
        val diamondKing = Card(suit = "♦", rank = "K")
        val spadeTwo = Card(suit = "♠", rank = "2")

        assertEquals(
            Card.NO_MATCH_SCORE,
            heartAce.match(arrayOf(diamondKing, spadeTwo))
        )
    }

    @Test
    fun sameSuitAloneDoesNotMatchThreeCards() {
        val heartAce = Card(suit = "♥", rank = "A")
        val heartTwo = Card(suit = "♥", rank = "2")
        val heartThree = Card(suit = "♥", rank = "3")

        assertEquals(
            Card.NO_MATCH_SCORE,
            heartAce.match(arrayOf(heartTwo, heartThree))
        )
    }

    @Test
    fun onlyTwoCardsDoNotMatchInThreeCardGame() {
        val heartAce = Card(suit = "♥", rank = "A")
        val spadeAce = Card(suit = "♠", rank = "A")

        assertEquals(Card.NO_MATCH_SCORE, heartAce.match(arrayOf(spadeAce)))
    }

    @Test
    fun toStringCombinesSuitAndRank() {
        assertEquals("♥10", Card(suit = "♥", rank = "10").toString())
    }
}
