package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sicnu.edu.cn.yangwencai2024110144.myapplication.MainActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameRecord
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 多 Fragment 主界面的 UI 测试：
 *   1. 个人信息页显示标题与默认资料；
 *   2. 修改资料后能立即刷新，并且重新从文件加载后仍然存在；
 *   3. 底部导航可以在三个 Fragment 之间切换，牌数符合资源；
 *   4. “关于”菜单仍然可用。
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        PlayerRepository.resetForTesting(context)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun profileFragmentShowsTitleAndDefaultProfile() {
        onView(withText(context.getString(R.string.app_name))).check(matches(isDisplayed()))

        onView(withId(R.id.tv_name_value))
            .check(matches(withText(context.getString(R.string.student_name))))
        onView(withId(R.id.tv_student_id_value))
            .check(matches(withText(context.getString(R.string.student_id))))
        onView(withId(R.id.tv_gender_value))
            .check(matches(withText(context.getString(R.string.gender_male))))
        onView(withId(R.id.tv_high_score_two_value))
            .check(matches(withText(context.getString(R.string.score_points_format, 0))))
        onView(withId(R.id.tv_high_score_three_value))
            .check(matches(withText(context.getString(R.string.score_points_format, 0))))

        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
    }

    @Test
    fun editingProfileRefreshesProfileFragmentAfterSave() {
        onView(withId(R.id.btn_edit_profile)).perform(click())

        onView(withId(R.id.et_name)).perform(scrollTo(), replaceText("杨小才"))
        onView(withId(R.id.et_phone)).perform(scrollTo(), replaceText("13900139000"))
        onView(withId(R.id.et_email)).perform(scrollTo(), replaceText("xiaocai@example.com"))
        closeSoftKeyboard()

        onView(withId(R.id.btn_save)).perform(scrollTo(), click())

        onView(withId(R.id.tv_name_value)).check(matches(withText("杨小才")))
        onView(withId(R.id.tv_phone_value)).check(matches(withText("13900139000")))
        onView(withId(R.id.tv_email_value)).check(matches(withText("xiaocai@example.com")))
    }

    @Test
    fun savedProfileSurvivesRepositoryReload() {
        onView(withId(R.id.btn_edit_profile)).perform(click())
        onView(withId(R.id.et_name)).perform(scrollTo(), replaceText("持久化测试"))
        closeSoftKeyboard()
        onView(withId(R.id.btn_save)).perform(scrollTo(), click())

        onView(withId(R.id.tv_name_value)).check(matches(withText("持久化测试")))

        // 关闭界面并丢弃内存中的单例，模拟“退出应用后重新打开”。
        scenario.close()
        PlayerRepository.reloadForTesting(context)
        scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.tv_name_value)).check(matches(withText("持久化测试")))
    }

    @Test
    fun gameHistoryIsRestoredAfterRelaunch() {
        // 关闭当前界面，用真实文件模拟上一次运行留下的游戏历史。
        scenario.close()
        val historyStore = GameHistoryStore(context, GameHistoryStore.TWO_CARD_FILE)
        historyStore.add(
            GameRecord(
                score = 18,
                playedAt = System.currentTimeMillis(),
                cardCount = 16,
                cardsInMatch = 2
            ),
            emptyList()
        )

        scenario = ActivityScenario.launch(MainActivity::class.java)

        // 个人信息 Fragment 直接从历史文件计算最高分。
        onView(withId(R.id.tv_high_score_two_value))
            .check(matches(withText(context.getString(R.string.score_points_format, 18))))

        onView(withId(R.id.nav_two_cards)).perform(click())
        onView(withText(context.getString(R.string.history_summary_format, 18, 1)))
            .check(matches(isDisplayed()))

        onView(allOf(withId(R.id.btn_history), isDisplayed())).perform(click())
        onView(withText(context.getString(R.string.history_title_two))).check(matches(isDisplayed()))
        onView(withText(context.getString(R.string.history_close))).perform(click())
    }

    @Test
    fun bottomNavigationSwitchesBetweenThreeFragments() {
        onView(withId(R.id.nav_two_cards)).perform(click())
        onView(withText(context.getString(R.string.game_two_hint))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.card_grid), isDisplayed())).check { view, noViewFound ->
            if (noViewFound != null) throw noViewFound
            val recycler = view as RecyclerView
            assertEquals(
                context.resources.getInteger(R.integer.two_card_count),
                recycler.adapter?.itemCount
            )
        }

        onView(withId(R.id.nav_three_cards)).perform(click())
        onView(withText(context.getString(R.string.game_three_hint))).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.card_grid), isDisplayed())).check { view, noViewFound ->
            if (noViewFound != null) throw noViewFound
            val recycler = view as RecyclerView
            assertEquals(
                context.resources.getInteger(R.integer.game_card_count),
                recycler.adapter?.itemCount
            )
        }

        onView(withId(R.id.nav_profile)).perform(click())
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
    }

    @Test
    fun aboutMenuShowsTheAboutDialog() {
        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(context.getString(R.string.menu_about))).perform(click())

        onView(withText(context.getString(R.string.about_title))).check(matches(isDisplayed()))
    }

    @Test
    fun emptyNameShowsValidationError() {
        onView(withId(R.id.btn_edit_profile)).perform(click())

        onView(withId(R.id.et_name)).perform(scrollTo(), replaceText(""))
        closeSoftKeyboard()
        onView(withId(R.id.btn_save)).perform(scrollTo(), click())

        onView(withText(context.getString(R.string.error_name_required)))
            .check(matches(isDisplayed()))
    }
}
