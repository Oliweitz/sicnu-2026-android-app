package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view

import android.content.Context
import android.graphics.Outline
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.Card

/**
 * 单张牌的 View。
 *
 * 正面显示点数和花色，背面显示统一的菱形花纹；
 * 翻开、配对的切换带有绕 Y 轴的翻转动画。
 * 它只认识 [CardUiState]，不参与任何计分逻辑。
 */
class PlayingCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val backView = FrameLayout(context)
    private val frontView = FrameLayout(context)

    private val rankTopView = TextView(context)
    private val suitTopView = TextView(context)
    private val centerSuitView = TextView(context)
    private val rankBottomView = TextView(context)
    private val suitBottomView = TextView(context)

    private var faceUp = false
    private var boundState: CardUiState? = null

    init {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dp(CARD_CORNER_RADIUS))
            }
        }
        elevation = dp(2f)
        cameraDistance = 12 * resources.displayMetrics.density

        buildBackView()
        buildFrontView()

        foreground = ContextCompat.getDrawable(context, R.drawable.card_ripple)
        isClickable = true
        isFocusable = true
    }

    private fun buildBackView() {
        backView.setBackgroundResource(R.drawable.bg_card_back)
        val pattern = ImageView(context).apply {
            setImageResource(R.drawable.ic_card_back)
            scaleType = ImageView.ScaleType.CENTER
            contentDescription = null
        }
        backView.addView(
            pattern,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
        addView(backView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun buildFrontView() {
        frontView.setBackgroundResource(R.drawable.bg_card_front)
        frontView.visibility = INVISIBLE

        rankTopView.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
        suitTopView.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
        val topCorner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            addView(rankTopView)
            addView(suitTopView)
        }
        val topParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        topParams.gravity = Gravity.TOP or Gravity.START
        topParams.setMargins(dp(8), dp(6), 0, 0)
        frontView.addView(topCorner, topParams)

        rankBottomView.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
        suitBottomView.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
        val bottomCorner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            rotation = 180f
            addView(rankBottomView)
            addView(suitBottomView)
        }
        val bottomParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        bottomParams.gravity = Gravity.BOTTOM or Gravity.END
        bottomParams.setMargins(0, 0, dp(8), dp(6))
        frontView.addView(bottomCorner, bottomParams)

        centerSuitView.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 30f)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
        frontView.addView(
            centerSuitView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER)
        )

        addView(frontView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * 把模型传来的状态画到这张牌上。
     * 状态由 Controller 统一生成，View 不做任何判断或计分。
     */
    fun bind(state: CardUiState) {
        boundState = state
        updateContentDescription(state)
        isClickable = !state.isMatched
        isFocusable = !state.isMatched
        alpha = if (state.isMatched) 0.72f else 1f

        frontView.setBackgroundResource(
            when {
                state.isMatched -> R.drawable.bg_card_front_matched
                state.isChosen -> R.drawable.bg_card_front_chosen
                else -> R.drawable.bg_card_front
            }
        )

        if (state.isFaceUp == faceUp) {
            applyFace(state)
        } else {
            faceUp = state.isFaceUp
            flip(state)
        }
    }

    private fun flip(state: CardUiState) {
        animate().cancel()
        rotationY = 0f
        animate()
            .rotationY(90f)
            .setDuration(FLIP_HALF_DURATION)
            .withEndAction {
                applyFace(state)
                rotationY = -90f
                animate()
                    .rotationY(0f)
                    .setDuration(FLIP_HALF_DURATION)
                    .start()
            }
            .start()
    }

    private fun applyFace(state: CardUiState) {
        val textColor = if (state.suit == Card.validSuits[0] || state.suit == Card.validSuits[1]) {
            ContextCompat.getColor(context, R.color.card_red)
        } else {
            ContextCompat.getColor(context, R.color.card_black)
        }

        rankTopView.text = state.rank
        suitTopView.text = state.suit
        rankTopView.setTextColor(textColor)
        suitTopView.setTextColor(textColor)

        rankBottomView.text = state.rank
        suitBottomView.text = state.suit
        rankBottomView.setTextColor(textColor)
        suitBottomView.setTextColor(textColor)

        centerSuitView.text = state.suit
        centerSuitView.setTextColor(textColor)

        frontView.visibility = if (state.isFaceUp) VISIBLE else INVISIBLE
        backView.visibility = if (state.isFaceUp) INVISIBLE else VISIBLE
    }

    private fun updateContentDescription(state: CardUiState) {
        contentDescription = when {
            state.isMatched -> context.getString(
                R.string.card_desc_matched,
                state.index + 1,
                state.face
            )
            state.isChosen -> context.getString(
                R.string.card_desc_chosen,
                state.index + 1,
                state.face
            )
            else -> context.getString(R.string.card_desc_back, state.index + 1)
        }
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val CARD_CORNER_RADIUS = 12f
        private const val FLIP_HALF_DURATION = 90L
    }
}
