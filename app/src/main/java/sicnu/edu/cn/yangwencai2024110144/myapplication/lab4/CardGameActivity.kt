package sicnu.edu.cn.yangwencai2024110144.myapplication.lab4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityCardGameBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.controller.CardGameController
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.CardMatchingGame
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardAdapter
import sicnu.edu.cn.yangwencai2024110144.myapplication.player.PlayerRepository

/**
 * 三张牌翻牌游戏 Activity。
 *
 * 这里是 Controller 与 Android 界面之间的接线层：
 * 创建 RecyclerView 和适配器，把牌的点击交给 Controller，
 * 把分数和游戏结束事件显示出来。
 *
 * 牌数由整数资源决定：竖屏 24 张，横屏 12 张；
 * 旋转屏幕后 Activity 重新创建，会自动按新方向读取牌数和每行列数。
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

        val cardCount = resources.getInteger(R.integer.game_card_count)
        val adapter = CardAdapter()
        binding.cardGrid.layoutManager =
            GridLayoutManager(this, resources.getInteger(R.integer.game_grid_span_count))
        binding.cardGrid.adapter = adapter
        // 翻牌动画由 PlayingCardView 自己完成，关闭 RecyclerView 的默认淡入淡出动画。
        binding.cardGrid.itemAnimator = null

        controller = CardGameController(
            view = adapter,
            modelFactory = { CardMatchingGame(cardCount) },
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
