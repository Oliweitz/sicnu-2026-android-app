package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import sicnu.edu.cn.yangwencai2024110144.myapplication.MainActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 主界面 UI 测试：
 *   1. 验证标题和默认玩家信息正确显示；
 *   2. 走一遍“修改个人信息 -> 保存 -> 回到主页自动刷新”的流程。
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        PlayerRepository.resetForTesting()
        PlayerRepository.ensureInitialized(context)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun mainScreenShowsTitleAndDefaultProfile() {
        onView(withText(context.getString(R.string.app_name))).check(matches(isDisplayed()))

        onView(withId(R.id.tv_name_value))
            .check(matches(withText(context.getString(R.string.student_name))))
        onView(withId(R.id.tv_student_id_value))
            .check(matches(withText(context.getString(R.string.student_id))))
        onView(withId(R.id.tv_gender_value))
            .check(matches(withText(context.getString(R.string.gender_male))))
        onView(withId(R.id.tv_high_score_value))
            .check(matches(withText(context.getString(R.string.score_points_format, 0))))

        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_open_game)).check(matches(isDisplayed()))
    }

    @Test
    fun editingProfileRefreshesMainScreenAfterSave() {
        onView(withId(R.id.btn_edit_profile)).perform(click())

        onView(withId(R.id.et_name)).perform(replaceText("杨小才"))
        onView(withId(R.id.et_phone)).perform(replaceText("13900139000"))
        onView(withId(R.id.et_email)).perform(replaceText("xiaocai@example.com"))

        // 保存按钮固定在底部操作栏，键盘弹出时仍位于键盘上方，可以直接点击。
        onView(withId(R.id.btn_save)).perform(click())

        onView(withId(R.id.tv_name_value)).check(matches(withText("杨小才")))
        onView(withId(R.id.tv_phone_value)).check(matches(withText("13900139000")))
        onView(withId(R.id.tv_email_value)).check(matches(withText("xiaocai@example.com")))
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

        onView(withId(R.id.et_name)).perform(replaceText(""))
        onView(withId(R.id.btn_save)).perform(click())

        onView(withText(context.getString(R.string.error_name_required)))
            .check(matches(isDisplayed()))
    }
}
