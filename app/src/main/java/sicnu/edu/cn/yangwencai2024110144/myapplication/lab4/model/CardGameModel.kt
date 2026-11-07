package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model

/**
 * 翻牌游戏模型对外暴露的最小接口。
 *
 * Controller 依赖接口而不是具体的 [CardMatchingGame]，
 * 这样单元测试可以注入假模型，也方便以后替换规则。
 */
interface CardGameModel {
    val score: Int
    val numberOfCards: Int
    val isGameOver: Boolean

    fun cardAtIndex(index: Int): Card

    fun chooseCardAtIndex(index: Int)

    /** 重新发牌并清零分数，用于“重置游戏”和“再来一局”。 */
    fun reset()
}
