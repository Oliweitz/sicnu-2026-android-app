package sicnu.edu.cn.yangwencai2024110144.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityMainBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.ProfileFragment
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.ThreeCardGameFragment
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.TwoCardGameFragment
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab7.MusicPlayerActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 实验六：多 Fragment 应用的主 Activity。
 *
 * 一个 Activity 承载三个 Fragment：
 *   1. [ProfileFragment]：个人信息，可修改并序列化保存；
 *   2. [TwoCardGameFragment]：2 张牌翻牌游戏，历史记录序列化保存；
 *   3. [ThreeCardGameFragment]：3 张牌翻牌游戏，历史记录序列化保存。
 *
 * 三个 Fragment 一次性 add 进来，切换标签时只 show / hide，
 * 因此切换标签不会丢失正在进行的牌局；旋转屏幕时 FragmentManager
 * 会自动恢复它们，当前标签页另外保存在 [KEY_CURRENT_TAG] 中。
 *
 * 实验七在底部导航增加了“音乐”入口。音乐播放器是独立的 [MusicPlayerActivity]，
 * 需要绑定 MusicPlayerService 拿 Binder，因此不作为 Fragment 放入容器。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentTag = TAG_PROFILE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerRepository.ensureInitialized(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            addFragments()
        }

        currentTag = savedInstanceState?.getString(KEY_CURRENT_TAG) ?: TAG_PROFILE

        // 先恢复底部导航的选中项，再注册监听，避免初始化时重复切换。
        binding.bottomNav.selectedItemId = menuItemForTag(currentTag)
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_music) {
                startActivity(Intent(this, MusicPlayerActivity::class.java))
                // 返回 false：音乐是独立页面，返回时底部导航仍停在上一个标签
                return@setOnItemSelectedListener false
            }
            val tag = tagForMenuItem(item.itemId) ?: return@setOnItemSelectedListener false
            showFragment(tag)
            true
        }
        showFragment(currentTag)
    }

    /** 首次创建时把三个 Fragment 都加入容器，只显示个人信息页。 */
    private fun addFragments() {
        val profileFragment = ProfileFragment()
        val twoCardFragment = TwoCardGameFragment()
        val threeCardFragment = ThreeCardGameFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, profileFragment, TAG_PROFILE)
            .add(R.id.fragment_container, twoCardFragment, TAG_TWO_CARDS)
            .add(R.id.fragment_container, threeCardFragment, TAG_THREE_CARDS)
            .hide(twoCardFragment)
            .hide(threeCardFragment)
            .commitNow()
    }

    /** 只显示指定标签对应的 Fragment，其余隐藏，保证游戏状态不丢失。 */
    private fun showFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction().setReorderingAllowed(true)
        FRAGMENT_TAGS.forEach { candidate ->
            val fragment = supportFragmentManager.findFragmentByTag(candidate) ?: return@forEach
            if (candidate == tag) transaction.show(fragment) else transaction.hide(fragment)
        }
        transaction.commitNow()
        currentTag = tag
    }

    private fun tagForMenuItem(itemId: Int): String? {
        return when (itemId) {
            R.id.nav_profile -> TAG_PROFILE
            R.id.nav_two_cards -> TAG_TWO_CARDS
            R.id.nav_three_cards -> TAG_THREE_CARDS
            else -> null
        }
    }

    private fun menuItemForTag(tag: String): Int {
        return when (tag) {
            TAG_TWO_CARDS -> R.id.nav_two_cards
            TAG_THREE_CARDS -> R.id.nav_three_cards
            else -> R.id.nav_profile
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_TAG, currentTag)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 菜单里的“关于”对话框；文案全部来自字符串资源。 */
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about_title)
            .setMessage(
                getString(
                    R.string.about_message,
                    getString(R.string.app_name),
                    getString(R.string.student_name),
                    getString(R.string.student_id)
                )
            )
            .setPositiveButton(R.string.confirm, null)
            .show()
    }

    private companion object {
        const val TAG_PROFILE = "profile"
        const val TAG_TWO_CARDS = "two_cards"
        const val TAG_THREE_CARDS = "three_cards"

        const val KEY_CURRENT_TAG = "current_tag"

        val FRAGMENT_TAGS = listOf(TAG_PROFILE, TAG_TWO_CARDS, TAG_THREE_CARDS)
    }
}
