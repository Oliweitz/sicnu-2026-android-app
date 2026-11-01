package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityCardGameBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.controller.CardGameController
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 翻牌游戏 Activity。
 *
 * 这里是 Controller 与 Android 界面之间的接线层：
 * 创建 Controller、把牌的点击交给它、把分数和游戏结束事件显示出来。
 * 游戏规则全部在 Model 中，界面刷新和交互调度在 Controller 中。
 */
class CardGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardGameBinding
    private lateinit var controller: CardGameController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlayerRepository.ensureInitialized(this)

        binding = ActivityCardGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        controller = CardGameController(
            view = binding.cardGrid,
            onScoreChanged = { score ->
                binding.tvScore.text = getString(R.string.score_value_format, score)
                PlayerRepository.recordHighScore(score)
            },
            onGameOver = ::showGameOverDialog
        )

        binding.btnReset.setOnClickListener { controller.startNewGame() }
        binding.btnExit.setOnClickListener { finish() }
    }

    private fun showGameOverDialog(score: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.game_over_title)
            .setMessage(getString(R.string.game_over_message, score))
            .setPositiveButton(R.string.play_again) { _, _ -> controller.startNewGame() }
            .setNegativeButton(R.string.exit) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}
