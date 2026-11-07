package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 翻牌游戏 UI 测试：
 *   1. RecyclerView 的牌数、列数按当前方向读取资源（竖屏 24 张 / 横屏 12 张）；
 *   2. 翻一张牌扣 1 分，重置后牌面与分数都恢复初始状态。
 */
@RunWith(AndroidJUnit4::class)
class CardGameActivityTest {

    private lateinit var scenario: ActivityScenario<CardGameActivity>

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        PlayerRepository.resetForTesting()
        PlayerRepository.ensureInitialized(context)
        scenario = ActivityScenario.launch(CardGameActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun newGameUsesRecyclerViewWithCardsFromResources() {
        scenario.onActivity { activity ->
            val recycler = activity.findViewById<RecyclerView>(R.id.card_grid)
            val expectedCount = activity.resources.getInteger(R.integer.game_card_count)
            val expectedSpanCount = activity.resources.getInteger(R.integer.game_grid_span_count)

            assertEquals(expectedCount, recycler.adapter?.itemCount ?: -1)
            assertEquals(expectedSpanCount, (recycler.layoutManager as GridLayoutManager).spanCount)
        }

        onView(withId(R.id.tv_score))
            .check(matches(withText(context.getString(R.string.score_value_format, 0))))
        onView(withId(R.id.card_1)).check(
            matches(withContentDescription(context.getString(R.string.card_desc_back, 1)))
        )
    }

    @Test
    fun choosingCardCostsOnePointAndResetClearsTheBoard() {
        onView(withId(R.id.card_1)).perform(click())
        onView(withId(R.id.tv_score))
            .check(matches(withText(context.getString(R.string.score_value_format, -1))))

        onView(withId(R.id.btn_reset)).perform(click())

        onView(withId(R.id.tv_score))
            .check(matches(withText(context.getString(R.string.score_value_format, 0))))
        onView(withId(R.id.card_1)).check(
            matches(withContentDescription(context.getString(R.string.card_desc_back, 1)))
        )
    }
}
