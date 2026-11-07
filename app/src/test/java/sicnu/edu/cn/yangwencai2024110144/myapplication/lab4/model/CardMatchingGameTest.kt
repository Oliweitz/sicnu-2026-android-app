package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 三张牌翻牌游戏模型测试。
 * 使用 withCards 构造确定的牌组，避免随机发牌导致断言时好时坏。
 */
class CardMatchingGameTest {

    @Test
    fun newGameStartsWithZeroScoreAndHiddenCards() {
        val game = CardMatchingGame(24)

        assertEquals(24, game.numberOfCards)
        assertEquals(0, game.score)
        assertFalse(game.isGameOver)

        for (index in 0 until game.numberOfCards) {
            val card = game.cardAtIndex(index)
            assertFalse("第 $index 张牌不应已翻开", card.isChosen)
            assertFalse("第 $index 张牌不应已配对", card.isMatched)
        }
    }

    @Test
    fun matchingThreeCardsOfSameRankAwardsBonus() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        assertEquals(-1, game.score)

        game.chooseCardAtIndex(1)
        assertEquals(-2, game.score)
        assertFalse(game.cardAtIndex(0).isMatched)

        game.chooseCardAtIndex(2)
        // 三次翻牌共 -3；配对得 4 × 4 = 16，最终 13
        assertEquals(13, game.score)
        assertTrue(game.cardAtIndex(0).isMatched)
        assertTrue(game.cardAtIndex(1).isMatched)
        assertTrue(game.cardAtIndex(2).isMatched)
        assertTrue(game.isGameOver)
    }

    @Test
    fun mismatchPenalizesAndFlipsAllThreeCardsBack() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "K"),
                Card(suit = "♠", rank = "2")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)

        // -1 -1 -1（翻三张）- 2（不匹配）= -5
        assertEquals(-5, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
        assertFalse(game.cardAtIndex(1).isChosen)
        assertFalse(game.cardAtIndex(2).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
        assertTrue(game.isGameOver)
    }

    @Test
    fun twoChosenCardsAreNotJudgedUntilTheThirdIsChosen() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        assertEquals(-2, game.score)
        assertTrue(game.cardAtIndex(0).isChosen)
        assertTrue(game.cardAtIndex(1).isChosen)
        assertFalse(game.cardAtIndex(0).isMatched)
        assertFalse(game.isGameOver)
    }

    @Test
    fun tappingChosenCardAgainDoesNotCostPoints() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "A")
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
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)
        val scoreAfterMatch = game.score

        game.chooseCardAtIndex(0)

        assertEquals(scoreAfterMatch, game.score)
        assertTrue(game.cardAtIndex(0).isMatched)
    }

    @Test
    fun gameIsOverWhenNoAvailableTripleRemains() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "K"),
                Card(suit = "♣", rank = "2")
            )
        )

        assertFalse(game.hasAvailableMatch())
        assertTrue(game.isGameOver)
    }

    @Test
    fun resetRedealsCardsAndClearsScore() {
        val game = CardMatchingGame.withCards(
            listOf(
                Card(suit = "♥", rank = "A"),
                Card(suit = "♦", rank = "A"),
                Card(suit = "♠", rank = "A")
            )
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)
        assertTrue(game.isGameOver)

        game.reset()

        assertEquals(3, game.numberOfCards)
        assertEquals(0, game.score)
        for (index in 0 until game.numberOfCards) {
            assertFalse(game.cardAtIndex(index).isChosen)
            assertFalse(game.cardAtIndex(index).isMatched)
        }
        assertFalse(game.isGameOver)
    }
}
