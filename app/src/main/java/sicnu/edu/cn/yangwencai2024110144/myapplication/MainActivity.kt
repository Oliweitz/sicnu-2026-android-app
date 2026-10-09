package sicnu.edu.cn.yangwencai2024110144.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.AdvisorActivity

/**
 * 多语言版 Hello World
 *
 * 一、界面不用布局文件，全部由代码创建（纯代码实现）。
 *     根布局、国旗图片、文字、按钮都在 onCreate 里 new 出来，
 *     最后 setContentView(rootLayout) 装上去。
 *
 * 二、三种语言放在三套资源目录里，由系统按当前语言自动挑选：
 *     文字  res/values/strings.xml      中文（默认）
 *           res/values-en/strings.xml   英文
 *           res/values-fr/strings.xml   法文
 *     国旗  res/drawable-nodpi/flag.png       中国国旗
 *           res/drawable-en-nodpi/flag.png    英国国旗
 *           res/drawable-fr-nodpi/flag.png    法国国旗
 *     也就是说 R.drawable.flag 同一个名字，在三种语言下会解析成三面不同的国旗。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 根布局：竖直排列，内容整体居中，四周留点边距
        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        rootLayout.setPadding(dp(20), dp(20), dp(20), dp(20))

        // 国旗图片：跟随系统语言自动换成对应国家的国旗
        val ivFlag = ImageView(this)
        ivFlag.setImageResource(R.drawable.flag)
        ivFlag.scaleType = ImageView.ScaleType.FIT_CENTER
        ivFlag.layoutParams = LinearLayout.LayoutParams(dp(180), dp(120))

        // 问候语（宽高都用 wrap_content，这样根布局的 gravity=CENTER 才能把它居中）
        val tvHello = TextView(this)
        tvHello.text = getString(R.string.hello)
        tvHello.textSize = 30f
        tvHello.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(20)
        }

        // 按钮：点一下就把问候语换成"我被点击了！"
        val btnClick = Button(this)
        btnClick.text = getString(R.string.click_me)
        btnClick.layoutParams = LinearLayout.LayoutParams(dp(160), dp(60)).apply {
            topMargin = dp(24)
        }
        btnClick.setOnClickListener {
            tvHello.text = getString(R.string.clicked)
        }

        // 通往实验二的入口（同样由代码创建）
        val btnAdvisor = Button(this)
        btnAdvisor.text = getString(R.string.goto_advisor)
        btnAdvisor.layoutParams = LinearLayout.LayoutParams(dp(160), dp(60)).apply {
            topMargin = dp(24)
        }
        btnAdvisor.setOnClickListener {
            startActivity(Intent(this, AdvisorActivity::class.java))
        }

        // 把控件依次加进根布局
        rootLayout.addView(ivFlag)
        rootLayout.addView(tvHello)
        rootLayout.addView(btnClick)
        rootLayout.addView(btnAdvisor)

        setContentView(rootLayout)
    }

    /** px 和 dp 的换算，让控件在不同分辨率的手机上一样大 */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
