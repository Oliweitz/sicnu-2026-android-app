package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.controller

import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.CardGameModel
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.CardMatchingGame
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardGameView
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardUiState

/**
 * 翻牌游戏的控制器（Controller 层）。
 *
 * 职责：
 *   1. 接受 View 传回来的点击事件；
 *   2. 调用 Model 处理规则、更新分数；
 *   3. 把 Model 的最新状态转换成 [CardUiState] 交给 View 显示；
 *   4. 提供重新开始一局游戏的入口。
 *
 * 这个类不依赖任何 Android API，可以直接用 JVM 单元测试验证。
 */
class CardGameController(
    private val view: CardGameView,
    private val modelFactory: () -> CardGameModel = { CardMatchingGame(DEFAULT_CARD_COUNT) },
    private val onScoreChanged: (Int) -> Unit = {},
    private val onGameOver: (Int) -> Unit = {}
) {
    private var model: CardGameModel = modelFactory()

    var isGameOver: Boolean = false
        private set

    init {
        view.onCardSelected = ::chooseCard
        render()
    }

    /**
     * 处理一次翻牌。
     * 越界的序号、已经配对的牌、以及游戏结束后的点击都被忽略。
     */
    fun chooseCard(index: Int) {
        if (isGameOver) return
        if (index !in 0 until model.numberOfCards) return

        val card = model.cardAtIndex(index)
        if (card.isMatched) return

        model.chooseCardAtIndex(index)
        render()

        if (model.isGameOver) {
            isGameOver = true
            onGameOver(model.score)
        }
    }

    /** 重新开始一局：换一副新牌，分数归零，清空游戏结束状态。 */
    fun startNewGame() {
        model = modelFactory()
        isGameOver = false
        render()
    }

    private fun render() {
        val cards = (0 until model.numberOfCards).map { index ->
            val card = model.cardAtIndex(index)
            CardUiState(
                index = index,
                suit = card.suit,
                rank = card.rank,
                isChosen = card.isChosen,
                isMatched = card.isMatched
            )
        }
        view.renderCards(cards)
        onScoreChanged(model.score)
    }

    companion object {
        /** 4 × 4 的牌桌，一局共 16 张牌。 */
        const val DEFAULT_CARD_COUNT = 16
    }
}
