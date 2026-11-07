package sicnu.edu.cn.yangwencai2024110144.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityMainBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.CardGameActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.PersonInfoActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.labelRes
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.showAvatar

/**
 * 实验四：翻牌游戏应用的主 Activity。
 *
 * 显示玩家信息（姓名、性别、学号、电话、邮箱、爱好、当前最高分），
 * 并通过两个按钮进入信息修改和翻牌游戏界面。
 *
 * 数据只放在 [PlayerRepository] 中，返回本界面时在 onResume 里重新读取，
 * 所以修改姓名等信息后无需借助 Intent 回传。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerRepository.ensureInitialized(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 把布局里的 MaterialToolbar 交给系统，选项菜单才能显示在右上角。
        setSupportActionBar(binding.toolbar)

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, PersonInfoActivity::class.java))
        }
        binding.btnOpenGame.setOnClickListener {
            startActivity(Intent(this, CardGameActivity::class.java))
        }
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

    override fun onResume() {
        super.onResume()
        renderProfile()
    }

    /** 把仓库里的最新数据同步到各个 TextView。 */
    private fun renderProfile() {
        val profile = PlayerRepository.current

        binding.ivAvatar.showAvatar(profile.avatarUri)
        binding.tvNameValue.text = profile.name
        binding.tvGenderValue.text = getString(profile.gender.labelRes())
        binding.tvStudentIdValue.text = profile.studentId
        binding.tvPhoneValue.text = profile.phone
        binding.tvEmailValue.text = profile.email

        binding.tvHobbiesValue.text = if (profile.hobbies.isEmpty()) {
            getString(R.string.hobbies_none)
        } else {
            profile.hobbies
                .sortedBy { it.ordinal }
                .joinToString(getString(R.string.list_separator)) { getString(it.labelRes()) }
        }

        binding.tvHighScoreValue.text =
            getString(R.string.score_points_format, profile.highScore)
    }
}
