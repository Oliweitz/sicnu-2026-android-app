package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Card 的配对规则单元测试。
 * Card 不依赖 Android，因此这些测试在本地 JVM 上直接运行。
 */
class CardTest {

    @Test
    fun sameRankScoresRankMatch() {
        val heartAce = Card(suit = "♥", rank = "A")
        val spadeAce = Card(suit = "♠", rank = "A")

        assertEquals(Card.RANK_MATCH_SCORE, heartAce.match(arrayOf(spadeAce)))
    }

    @Test
    fun sameSuitOnlyScoresSuitMatch() {
        val heartAce = Card(suit = "♥", rank = "A")
        val heartTwo = Card(suit = "♥", rank = "2")

        assertEquals(Card.SUIT_MATCH_SCORE, heartAce.match(arrayOf(heartTwo)))
    }

    @Test
    fun differentRankAndSuitDoesNotMatch() {
        val heartAce = Card(suit = "♥", rank = "A")
        val spadeKing = Card(suit = "♠", rank = "K")

        assertEquals(Card.NO_MATCH_SCORE, heartAce.match(arrayOf(spadeKing)))
    }

    @Test
    fun toStringCombinesSuitAndRank() {
        assertEquals("♥10", Card(suit = "♥", rank = "10").toString())
    }
}
