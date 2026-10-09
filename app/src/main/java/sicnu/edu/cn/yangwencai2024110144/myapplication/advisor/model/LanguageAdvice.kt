package sicnu.edu.cn.yangwencai2024110144.myapplication.advisor.model

import androidx.annotation.StringRes

/**
 * 模型层：顾问给出的一条建议——一句该语言的问候语 + 一条文化小贴士。
 *
 * 注意这里存的仍然是资源 ID 而不是 String：
 * 模型层负责"查到什么"，视图层负责"怎么显示"。
 */
data class LanguageAdvice(
    @param:StringRes val greetingRes: Int,
    @param:StringRes val tipRes: Int,
)
