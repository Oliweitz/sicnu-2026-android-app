package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view

/**
 * 翻牌游戏界面的抽象（View 层）。
 *
 * Controller 只依赖这个接口，配合 [CardUiState] 同步界面；
 * 单元测试里可以用一个记录数据的假 View 来验证 Controller。
 */
interface CardGameView {
    /** 由 Controller 设置；View 检测到点击时把牌的序号回调出去。 */
    var onCardSelected: ((Int) -> Unit)?

    /** 用最新状态重绘全部牌。 */
    fun renderCards(cards: List<CardUiState>)
}
