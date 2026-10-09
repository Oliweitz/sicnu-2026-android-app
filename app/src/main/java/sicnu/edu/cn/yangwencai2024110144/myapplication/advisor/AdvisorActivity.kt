package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.R
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.controller.AdviserController
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.Language
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.LanguageAdvice
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model.LanguageAdviser
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.view.AdviserView
import sicnu.edu.cn.yangwencai2024110144.myapplication.databinding.ActivityAdvisorBinding

/**
 * 实验二：UI 代码与 MVC
 *
 * 本类在 MVC 里扮演 V（视图）：
 *   一、界面骨架放在 activity_advisor.xml，用 ViewBinding 拿到控件引用，不再写 findViewById；
 *   二、区一的 TextView 由代码 new 出来，加进 ScrollView 里那层 LinearLayout，
 *       因此必须用"ScrollView 嵌套 LinearLayout"的结构才装得下、滚得动；
 *   三、区二的"确定"按钮只做一件事——把事件转交给 [AdviserController]，
 *       视图自己不查表、不做判断，只负责把模型返回的资源 ID 取成文字显示。
 *
 * 全类没有一句写死的界面文字，凡是要显示的内容都走 getString(R.string.xxx)。
 */
class AdvisorActivity : AppCompatActivity(), AdviserView {

    private lateinit var binding: ActivityAdvisorBinding
    private lateinit var controller: AdviserController

    /** 区一已经添加了多少个 TextView，用来给新条目编号 */
    private var addedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvisorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 视图把自己交给控制器；模型由控制器自己持有
        controller = AdviserController(this)

        setupSection1()
        setupSection2()
    }

    // ==================== 区一：动态添加 TextView ====================

    /** 点一次按钮，就用代码造一个 TextView 追加到滚动容器里 */
    private fun setupSection1() {
        binding.btnAddText.setOnClickListener {
            addedCount++
            binding.containerDynamic.addView(createNumberedTextView(addedCount))

            // 新条目加在末尾，让它自动滚到能看见的位置
            binding.scrollDynamic.post {
                binding.scrollDynamic.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    /** 纯代码造一个 TextView——文字、字号、内外边距全部在这里设定 */
    private fun createNumberedTextView(number: Int): TextView = TextView(this).apply {
        text = getString(R.string.dynamic_item_format, number)
        textSize = 18f
        setPadding(0, dp(8), 0, dp(8))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // ==================== 区二：语言文化顾问 ====================

    /** 把模型里的语言填进 Spinner，并把按钮事件转交给控制器 */
    private fun setupSection2() {
        // Spinner 的选项来自模型层，视图只是把资源 ID 取成文字
        val names = LanguageAdviser.languages.map { getString(it.displayNameRes) }
        binding.spinnerLanguage.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            names
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // 视图不处理业务，只把点击转给控制器
        binding.btnConfirm.setOnClickListener {
            controller.onConfirmClicked()
        }
    }

    // ==================== AdviserView：视图对控制层的承诺 ====================

    override fun selectedLanguage(): Language =
        LanguageAdviser.languageAt(binding.spinnerLanguage.selectedItemPosition)

    override fun showAdvice(advice: LanguageAdvice) {
        binding.tvAdvice.text = getString(
            R.string.advice_format,
            getString(advice.greetingRes),
            getString(advice.tipRes)
        )
    }

    /** px 和 dp 的换算，让控件在不同分辨率的手机上一样大 */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
