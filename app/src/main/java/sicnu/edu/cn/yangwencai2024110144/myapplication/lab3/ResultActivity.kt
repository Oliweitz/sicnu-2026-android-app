package sicnu.edu.cn.yangwencai2024110144.myapplication.lab3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityResultBinding

/**
 * 二级界面二：输入内容并返回给 MainActivity。
 *
 * 返回值靠两个动作配合：
 *   1. setResult(resultCode, Intent)：把数据装进一个空的 Intent；
 *   2. finish()：结束页面，系统把 resultCode 和这个 Intent 回传给启动方。
 * 点“取消”或直接按返回键时没有调用 setResult，系统默认返回 RESULT_CANCELED。
 */
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnConfirm.setOnClickListener { returnInput() }
    }

    /** 校验输入并把内容带回 MainActivity */
    private fun returnInput() {
        val content = binding.etInput.text.toString().trim()

        // 内容为空时不返回，用 EditText 自带的错误提示提醒用户
        if (content.isEmpty()) {
            binding.etInput.error = getString(R.string.result_empty_error)
            return
        }

        val resultIntent = Intent().apply {
            putExtra(EXTRA_RESULT, content)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        /** 返回内容的键，MainActivity 用同名常量读取 */
        const val EXTRA_RESULT = "extra_result"
    }
}
