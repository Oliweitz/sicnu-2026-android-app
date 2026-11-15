package sicnu.edu.cn.yangwencai2024110144.myapplication.lab6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.FragmentCardGameBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.controller.CardGameController
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.model.CardMatchingGame
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab4.view.CardAdapter
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameHistoryStore
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab6.storage.GameRecord
import java.text.DateFormat
import java.util.Date

/**
 * 第 2、3 个 Fragment 的共同实现。
 *
 * 子类只需要声明“一次配对几张牌、一局多少张牌、每行几张、文案和历史文件名”，
 * 发牌、翻牌、计分仍然复用实验五的 Model / View / Controller：
 *   - [CardMatchingGame] 按 [cardsInMatch] 支持 2 张或 3 张配对；
 *   - [CardAdapter] 负责 RecyclerView 牌桌；
 *   - [CardGameController] 负责把点击交给 Model 并刷新界面。
 *
 * 一局结束时把 [GameRecord] 追加到 [GameHistoryStore]，
 * 由 ObjectOutputStream 序列化写入文件；下次打开应用时再反序列化恢复。
 */
abstract class BaseCardGameFragment : Fragment() {

    /** 游戏提示文案，例如“每次翻开 2 张牌……”。 */
    @get:StringRes
    protected abstract val gameHintRes: Int

    /** 历史记录对话框的标题。 */
    @get:StringRes
    protected abstract val historyTitleRes: Int

    /** 本玩法一局的牌数（竖屏 / 横屏由资源覆盖）。 */
    @get:IntegerRes
    protected abstract val cardCountRes: Int

    /** 牌桌每行显示几张牌。 */
    @get:IntegerRes
    protected abstract val gridSpanRes: Int

    /** 一次配对需要翻开的牌数：2 或 3。 */
    protected abstract val cardsInMatch: Int

    /** 本玩法的历史记录文件名。 */
    protected abstract val historyFileName: String

    private var _binding: FragmentCardGameBinding? = null
    private val binding: FragmentCardGameBinding
        get() = checkNotNull(_binding) { "Fragment 视图尚未创建或已经销毁" }

    private lateinit var controller: CardGameController
    private lateinit var historyStore: GameHistoryStore
    private var history: List<GameRecord> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyStore = GameHistoryStore(requireContext(), historyFileName)
        history = historyStore.load()

        binding.tvGameHint.setText(gameHintRes)

        val adapter = CardAdapter()
        binding.cardGrid.layoutManager =
            GridLayoutManager(requireContext(), resources.getInteger(gridSpanRes))
        binding.cardGrid.adapter = adapter
        // 翻牌动画由 PlayingCardView 自己完成，关闭 RecyclerView 的默认淡入淡出动画。
        binding.cardGrid.itemAnimator = null

        controller = CardGameController(
            view = adapter,
            modelFactory = {
                CardMatchingGame(resources.getInteger(cardCountRes), cardsInMatch)
            },
            onScoreChanged = ::renderScore,
            onGameOver = ::onGameOver
        )

        binding.btnReset.setOnClickListener { controller.startNewGame() }
        binding.btnHistory.setOnClickListener { showHistoryDialog() }

        renderHistorySummary()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun renderScore(score: Int) {
        binding.tvScore.text = getString(R.string.score_value_format, score)
        renderHistorySummary()
    }

    /** 底部一行小字：历史最高分和已完成的局数，历史记录会被恢复。 */
    private fun renderHistorySummary() {
        if (history.isEmpty()) {
            binding.tvHistorySummary.setText(R.string.history_summary_empty)
        } else {
            val best = history.maxOf { it.score }.coerceAtLeast(0)
            binding.tvHistorySummary.text =
                getString(R.string.history_summary_format, best, history.size)
        }
    }

    /**
     * 一局结束：先记录成绩并立即写入序列化文件，再弹对话框。
     * 这样即使玩家马上退出应用，这一局的成绩也不会丢。
     */
    private fun onGameOver(score: Int) {
        val previousBest = (history.maxOfOrNull { it.score } ?: 0).coerceAtLeast(0)
        val record = GameRecord(
            score = score,
            playedAt = System.currentTimeMillis(),
            cardCount = resources.getInteger(cardCountRes),
            cardsInMatch = cardsInMatch
        )
        history = historyStore.add(record, history)
        renderHistorySummary()
        showGameOverDialog(score, isNewRecord = score > previousBest)
    }

    private fun showGameOverDialog(score: Int, isNewRecord: Boolean) {
        val message = if (isNewRecord) {
            getString(R.string.game_over_new_record, score)
        } else {
            getString(R.string.game_over_message, score)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.game_over_title)
            .setMessage(message)
            .setPositiveButton(R.string.play_again) { _, _ -> controller.startNewGame() }
            .setNegativeButton(R.string.history_button) { _, _ -> showHistoryDialog() }
            .setCancelable(false)
            .show()
    }

    private fun showHistoryDialog() {
        val message = if (history.isEmpty()) {
            getString(R.string.history_empty)
        } else {
            val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            history.joinToString(separator = "\n") { record ->
                getString(
                    R.string.history_record_format,
                    formatter.format(Date(record.playedAt)),
                    record.score,
                    record.cardCount
                )
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(historyTitleRes)
            .setMessage(message)
            .setPositiveButton(R.string.history_close, null)
            .show()
    }
}
