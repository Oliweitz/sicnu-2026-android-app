package sicnu.edu.cn.yangwencai2024110144.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityMainBinding
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab3.CameraActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab3.ParamActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab3.ResultActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.lab3.StopwatchActivity

/**
 * 实验三：Intent 的用法与多 Activity 开发
 *
 * 本界面是程序入口，四个按钮对应四个二级 Activity：
 *   1. 参数传递：显式 Intent + putExtra，二级页面读出并显示参数；
 *   2. 返回结果：二级页面用 setResult 把输入内容带回，这里再显示；
 *   3. 调用相机：隐式 Intent（由二级页面发出），拍照后显示照片；
 *   4. 秒表：旋转屏幕后计时连续，演示 Activity 重建时的状态保存与恢复。
 *
 * 界面文字全部来自 strings.xml，代码中不写死任何显示文案。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * 接收活动二返回的结果。
     * registerForActivityResult 取代了旧的 startActivityForResult / onActivityResult，
     * 它在 Activity 重建后依然能收到结果，不会因为旋转屏幕而丢失。
     */
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringExtra(ResultActivity.EXTRA_RESULT)
            if (!text.isNullOrBlank()) {
                binding.tvReturnedResult.text = getString(R.string.main_result_format, text)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 显式 Intent：指明要启动哪个 Activity，并用 extra 正向传参
        binding.btnParam.setOnClickListener {
            val intent = Intent(this, ParamActivity::class.java).apply {
                putExtra(ParamActivity.EXTRA_SENDER, getString(R.string.student_name))
                putExtra(ParamActivity.EXTRA_STUDENT_ID, getString(R.string.student_id))
                putExtra(ParamActivity.EXTRA_MESSAGE, getString(R.string.param_message))
            }
            startActivity(intent)
        }

        // 2. 带着“期待结果”的方式启动，结果由上面的 resultLauncher 接收
        binding.btnResult.setOnClickListener {
            resultLauncher.launch(Intent(this, ResultActivity::class.java))
        }

        // 3. 相机与 4. 秒表都由对应的二级 Activity 自己完成
        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        binding.btnStopwatch.setOnClickListener {
            startActivity(Intent(this, StopwatchActivity::class.java))
        }
    }
}
