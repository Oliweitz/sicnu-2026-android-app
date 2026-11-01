package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 翻牌游戏 UI 测试：
 *   1. 一局开始应有 16 张牌、全部背面、分数为 0；
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
    fun newGameHasSixteenHiddenCardsAndZeroScore() {
        onView(withId(R.id.card_grid)).check(matches(hasChildCount(16)))
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
