package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 翻牌游戏模型测试。
 * 使用 withCards 构造确定的牌组，避免随机抽牌导致断言时好时坏。
 */
class CardMatchingGameTest {

    @Test
    fun newGameStartsWithZeroScoreAndSixteenHiddenCards() {
        val game = CardMatchingGame(16)

        assertEquals(16, game.numberOfCards)
        assertEquals(0, game.score)
        assertFalse(game.isGameOver)

        for (index in 0 until game.numberOfCards) {
            val card = game.cardAtIndex(index)
            assertFalse("第 $index 张牌不应已翻开", card.isChosen)
            assertFalse("第 $index 张牌不应已配对", card.isMatched)
        }
    }

    @Test
    fun matchingRankAwardsBonusAndChargesChooseCost() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score)

        game.chooseCardAtIndex(1)
        // 第一次 -1；配对得 4 × 4 = 16，再扣翻牌成本 1，最终 14
        assertEquals(14, game.score)
        assertTrue(game.cardAtIndex(0).isMatched)
        assertTrue(game.cardAtIndex(1).isMatched)
        assertTrue(game.isGameOver)
    }

    @Test
    fun matchingSuitScoresLessThanMatchingRank() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♥", rank = "2")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        // -1 + 1 × 4 - 1 = 2
        assertEquals(2, game.score)
        assertTrue(game.cardAtIndex(0).isMatched)
        assertTrue(game.cardAtIndex(1).isMatched)
    }

    @Test
    fun mismatchPenalizesAndFlipsFirstCardBack() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♠", rank = "K"),
                Card(suit = "♠", rank = "2")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        // -1（翻第一张）- 2（不匹配）- 1（翻第二张）= -4
        assertEquals(-4, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
        assertTrue(game.cardAtIndex(1).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
        assertFalse(game.cardAtIndex(1).isMatched)
    }

    @Test
    fun tappingChosenCardAgainDoesNotCostPoints() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♠", rank = "K")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun matchedCardCannotBeChosenAgain() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        val scoreAfterMatch = game.score

        game.chooseCardAtIndex(0)

        assertEquals(scoreAfterMatch, game.score)
        assertTrue(game.cardAtIndex(0).isMatched)
    }

    @Test
    fun gameIsOverWhenNoAvailableMatchRemains() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "2"),
                Card(suit = "♠", rank = "3"),
                Card(suit = "♣", rank = "4")
            )
        )

        assertFalse(game.hasAvailableMatch())
        assertTrue(game.isGameOver)
    }
}
