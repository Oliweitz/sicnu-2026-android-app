package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view

/**
 * View 层需要的一张牌的状态快照。
 *
 * 它不引用 Model 的 [sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.Card]，
 * 这样 View 只负责画界面，规则变化不会影响它。
 */
data class CardUiState(
    val index: Int,
    val suit: String,
    val rank: String,
    val isChosen: Boolean,
    val isMatched: Boolean
) {
    val isFaceUp: Boolean
        get() = isChosen || isMatched

    val face: String
        get() = suit + rank
}
