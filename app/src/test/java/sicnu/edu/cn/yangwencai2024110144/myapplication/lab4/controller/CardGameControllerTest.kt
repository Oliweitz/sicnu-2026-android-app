package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.controller

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.Card
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.CardMatchingGame
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardGameView
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardUiState

/**
 * Controller 测试：用假 View 记录渲染结果，验证交互、分数同步和重新开局。
 */
class CardGameControllerTest {

    private class RecordingView : CardGameView {
        override var onCardSelected: ((Int) -> Unit)? = null
        var cards: List<CardUiState> = emptyList()

        override fun renderCards(cards: List<CardUiState>) {
            this.cards = cards
        }
    }

    private fun newController(
        view: RecordingView,
        scores: MutableList<Int>,
        onGameOver: (Int) -> Unit = {}
    ): CardGameController {
        return CardGameController(
            view = view,
            modelFactory = {
                CardMatchingGame.withCards(
                    listOf(
                        Card(suit = "♥", rank = "A"),
                        Card(suit = "♠", rank = "A")
                    )
                )
            },
            onScoreChanged = { scores.add(it) },
            onGameOver = onGameOver
        )
    }

    @Test
    fun initializationRendersHiddenCardsAndZeroScore() {
        val view = RecordingView()
        val scores = mutableListOf<Int>()

        val controller = newController(view, scores)

        assertNotNull(view.onCardSelected)
        assertEquals(2, view.cards.size)
        assertTrue(view.cards.none { it.isFaceUp })
        assertEquals(listOf(0), scores)
        assertFalse(controller.isGameOver)
    }

    @Test
    fun chooseCardUpdatesViewAndScore() {
        val view = RecordingView()
        val scores = mutableListOf<Int>()
        val controller = newController(view, scores)

        view.onCardSelected?.invoke(0)

        assertEquals(-1, scores.last())
        assertTrue(view.cards[0].isChosen)
        assertFalse(view.cards[0].isMatched)

        view.onCardSelected?.invoke(1)

        assertEquals(14, scores.last())
        assertTrue(view.cards.all { it.isMatched })
        assertTrue(controller.isGameOver)
    }

    @Test
    fun gameOverIsReportedOnceAndFurtherClicksAreIgnored() {
        val view = RecordingView()
        val scores = mutableListOf<Int>()
        var gameOverScore: Int? = null
        val controller = newController(view, scores) { gameOverScore = it }

        view.onCardSelected?.invoke(0)
        view.onCardSelected?.invoke(1)

        assertEquals(14, gameOverScore)
        assertTrue(controller.isGameOver)

        val scoreEventsBefore = scores.size
        view.onCardSelected?.invoke(0)
        assertEquals(scoreEventsBefore, scores.size)
    }

    @Test
    fun startNewGameResetsCardsAndScore() {
        val view = RecordingView()
        val scores = mutableListOf<Int>()
        val controller = newController(view, scores)

        view.onCardSelected?.invoke(0)
        view.onCardSelected?.invoke(1)
        assertTrue(controller.isGameOver)

        controller.startNewGame()

        assertEquals(0, scores.last())
        assertTrue(view.cards.none { it.isFaceUp })
        assertFalse(controller.isGameOver)
    }

    @Test
    fun outOfRangeIndexIsIgnored() {
        val view = RecordingView()
        val scores = mutableListOf<Int>()
        newController(view, scores)

        val eventsBefore = scores.size
        view.onCardSelected?.invoke(99)

        assertEquals(eventsBefore, scores.size)
        assertTrue(view.cards.none { it.isFaceUp })
    }
}
