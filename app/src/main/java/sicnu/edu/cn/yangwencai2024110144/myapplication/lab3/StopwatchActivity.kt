package sicnu.edu.cn.yangwencai2024110144.myapplication.lab3

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityStopwatchBinding

/**
 * 二级界面四：秒表。
 *
 * 计时用 Chronometer，基准时间取 SystemClock.elapsedRealtime()：
 * 这个时钟从开机起单调递增，即使设备休眠也会继续走，适合做秒表。
 *
 * 旋转屏幕会销毁并重建 Activity，因此必须自己保存状态：
 *   - elapsedMs：已经累计的毫秒数；
 *   - running：旋转前是否正在计时。
 * 在 onSaveInstanceState 里写入，在 onCreate 里读回；
 * 如果旋转前正在计时，重建后用同一个“已用时间”设置基准并继续 start()，
 * 表盘就会无缝接着走，而不是从 0 开始。
 */
class StopwatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStopwatchBinding

    /** 已经累计的毫秒数（暂停时也保留） */
    private var elapsedMs = 0L

    /** 当前是否正在计时 */
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopwatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.chronometer.format = getString(R.string.chronometer_format)

        // 如果是旋转屏幕后重建，先取回旋转前的计时状态
        if (savedInstanceState != null) {
            elapsedMs = savedInstanceState.getLong(KEY_ELAPSED_MS, 0L)
            running = savedInstanceState.getBoolean(KEY_RUNNING, false)
        }

        // 用“当前时刻 - 已用时间”作为基准；无论之前是否在计时，显示值都正确
        binding.chronometer.base = SystemClock.elapsedRealtime() - elapsedMs
        if (running) {
            binding.chronometer.start()
        }

        binding.btnStart.setOnClickListener { startTiming() }
        binding.btnPause.setOnClickListener { pauseTiming() }
        binding.btnReset.setOnClickListener { resetTiming() }

        updateButtonState()
    }

    /**
     * Activity 被销毁前（旋转屏幕、进入后台被回收等）保存状态。
     * 注意：正在计时时要先把此刻的已用时间结算出来，
     * 否则重建后只能拿到上次暂停时留下的旧值。
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (running) {
            elapsedMs = SystemClock.elapsedRealtime() - binding.chronometer.base
        }
        outState.putLong(KEY_ELAPSED_MS, elapsedMs)
        outState.putBoolean(KEY_RUNNING, running)
    }

    /** 开始或继续计时 */
    private fun startTiming() {
        if (running) return

        binding.chronometer.base = SystemClock.elapsedRealtime() - elapsedMs
        binding.chronometer.start()
        running = true
        updateButtonState()
    }

    /** 暂停计时，把当前已用时间结算到 elapsedMs */
    private fun pauseTiming() {
        if (!running) return

        elapsedMs = SystemClock.elapsedRealtime() - binding.chronometer.base
        binding.chronometer.stop()
        binding.chronometer.base = SystemClock.elapsedRealtime() - elapsedMs
        running = false
        updateButtonState()
    }

    /** 停止并清零 */
    private fun resetTiming() {
        binding.chronometer.stop()
        elapsedMs = 0L
        running = false
        binding.chronometer.base = SystemClock.elapsedRealtime()
        updateButtonState()
    }

    /** 根据当前状态决定三个按钮是否可点 */
    private fun updateButtonState() {
        binding.btnStart.isEnabled = !running
        binding.btnPause.isEnabled = running
        binding.btnReset.isEnabled = running || elapsedMs > 0L
    }

    companion object {
        private const val KEY_ELAPSED_MS = "key_elapsed_ms"
        private const val KEY_RUNNING = "key_running"
    }
}
