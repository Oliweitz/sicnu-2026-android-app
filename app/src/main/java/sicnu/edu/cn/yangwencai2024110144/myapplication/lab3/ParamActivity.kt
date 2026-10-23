package sicnu.edu.cn.yangwencai2024110144.myapplication.lab3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityParamBinding

/**
 * 二级界面一：接收并显示 MainActivity 正向传来的参数。
 *
 * “正向传参”就是发送方在 Intent 里 putExtra，接收方用 getIntent() 取回。
 * 三个 extra 的键定义在 companion object 里，发送方和接收方引用同一组常量，
 * 避免两边各写一个字符串导致对不上。
 */
class ParamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 左上角箭头：结束当前 Activity，回到 MainActivity
        binding.toolbar.setNavigationOnClickListener { finish() }

        // getIntent() 就是启动本页面时传进来的那个 Intent；取不到时用占位文案
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: getString(R.string.value_missing)
        val studentId = intent.getStringExtra(EXTRA_STUDENT_ID) ?: getString(R.string.value_missing)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: getString(R.string.value_missing)

        binding.tvSender.text = getString(R.string.param_sender_format, sender)
        binding.tvStudentId.text = getString(R.string.param_id_format, studentId)
        binding.tvMessage.text = getString(R.string.param_message_format, message)
    }

    companion object {
        /** 发送方姓名 */
        const val EXTRA_SENDER = "extra_sender"

        /** 学号 */
        const val EXTRA_STUDENT_ID = "extra_student_id"

        /** 一段消息正文 */
        const val EXTRA_MESSAGE = "extra_message"
    }
}
